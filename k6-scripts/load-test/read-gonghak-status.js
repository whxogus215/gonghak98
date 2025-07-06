import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';

// --- 사용자 데이터 로드 ---
const users = new SharedArray('test_users', function () {
  let userData;
  try {
    const fileContent = open('../test-users.json');
    userData = JSON.parse(fileContent);
  } catch (e) {
    console.error(`[SharedArray] Error loading/parsing test-users.json: ${e}`);
    userData = [];
  }
  return userData;
});

export let options = {
  scenarios: {
    load_test_scenario: {
      executor: 'ramping-vus',
      startVUs: 0,          // 시작 VU 수
      stages: [
        // 3분 동안 사용자를 200명까지 서서히 늘립니다.
        // 이 구간에서 응답시간과 에러율이 언제부터 증가하는지 관찰할 수 있습니다.
        { duration: '3m', target: 50 },

        // 5분 동안 200명의 사용자 부하를 계속 유지합니다.
        // 이 구간이 가장 중요하며, WAS/DB의 CPU, Memory, DB Connection Pool 상태를
        // 그라파나로 집중 모니터링하여 병목 지점을 찾아냅니다.
        { duration: '5m', target: 50 },

        // 1분 동안 사용자를 서서히 줄이며 테스트를 종료합니다.
        { duration: '1m', target: 0 },
      ],
      gracefulRampDown: '30s', // VU 감소 시 30초 동안 부드럽게 감소
      tags: {
        test_type: 'load_test',
      }
    },
  },
  thresholds: {
    'http_req_duration{name:Optimized_Grades_Load}': ['p(95)<2000'],
    'http_req_failed': ['rate<0.05'],
  },
}

export default function () {
  if (users.length === 0) {
    console.error(`VU ${__VU}: [Default Function] No user data available. Skipping test iteration.`);
    return;
  }

  const user = users[(__VU - 1) % users.length];

  let loginPageRes = http.get('http://localhost:8080/user/login');

  let csrfToken = null;
  // --- CSRF 토큰 추출: 정규 표현식 사용 ---
  // <input type="hidden" name="_csrf" value="([^"]+)" /> 형태의 패턴을 찾습니다.
  const csrfMatch = loginPageRes.body.match(/<input type="hidden" name="_csrf" value="([^"]+)"\s*\/?>/);

  if (csrfMatch && csrfMatch[1]) { // 매칭된 결과가 있고, 첫 번째 캡처 그룹(value 값)이 있다면
    csrfToken = csrfMatch[1];
  }

  sleep(0.5); // 잠시 대기

  // --- Step 2: 로그인 요청 (POST) ---
  let loginRes = http.post('http://localhost:8080/user/login',
      `_csrf=${csrfToken}&username=${user.username}&password=${user.password}`,
      { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
  );

  check(loginRes, {
    '로그인 성공 (status 200)': (r) => r.status === 200,
    '로그인 후 페이지에 인증결과 포함': (r) => r.body.includes('인증정보 확인'),
  });

  if (!check(loginRes, { '로그인 성공 (status 200)': (r) => r.status === 200 })) {
    console.error(`VU ${__VU}: 로그인 실패 (사용자: ${user.username}), 상태: ${loginRes.status}. 응답: ${loginRes.body.substring(0, 200)}...`);
    return;
  }
  sleep(0.5);

  // --- Step 3: 인증현황 및 추천과목 리소스 GET 요청 ---
  let optimizedApiRes = http.get('http://localhost:8080/gonghak/status',
      {
        tags: { name: 'Optimized_Grades_Load' },
      }
  );

  check(optimizedApiRes, {
    '최적화 API 호출 성공 (status 200)': (r) => r.status === 200,
    '페이지에 인증결과 포함': (r) => r.body.includes('인증정보 확인'),
  });

  sleep(1);
}
