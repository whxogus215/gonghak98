import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';

// --- 사용자 데이터 로드 ---
const users = new SharedArray('test_users', function () {
  return JSON.parse(open('../test-users.json'));
});

// --- 사용자 기이수파일 데이터 로드 ---
const uploadFile = open('../test-completed-course-file.xlsx', 'b');

export let options = {
  scenarios: {
    load_test_scenario: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '3m', target: 15 },
        { duration: '5m', target: 15 },
        { duration: '1m', target: 0 },
      ],
      gracefulRampDown: '30s',
      tags: {
        test_type: 'load_test',
      }
    },
  },
  thresholds: {
    'http_req_duration{name:Excel_Upload_Request}': ['p(95)<2000'],
    'http_req_failed': ['rate<0.05'],
  },
};

export default function () {
  if (users.length === 0) {
    console.error(`VU ${__VU}: No user data available.`);
    return;
  }

  const user = users[(__VU - 1) % users.length];
  const jar = http.cookieJar();

  // --- Step 1: 최초 CSRF 토큰 획득 ---
  let loginPageRes = http.get('http://localhost:8080/user/login');
  let initialCsrfToken = loginPageRes.html('input[name=_csrf]').attr('value');

  sleep(0.5);

  // --- Step 2: 로그인 요청 ---
  let loginSuccessPageRes = http.post(
      'http://localhost:8080/user/login',
      `_csrf=${initialCsrfToken}&username=${user.username}&password=${user.password}`,
      {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        jar: jar,
      }
  );

  const loginCheckResult = check(loginSuccessPageRes, {
    '로그인 성공 (status 200)': (r) => r.status === 200,
  });

  if (!loginCheckResult) {
    console.error(`VU ${__VU}: 로그인 실패. Status: ${loginSuccessPageRes.status}`);
    return;
  }

  sleep(0.5);

  // --- [추가된 Step 3]: 파일 업로드 페이지로 이동 (GET 요청) ---
  let excelPageRes = http.get('http://localhost:8080/excel', {
    jar: jar, // 로그인 세션을 유지하기 위해 jar 전달
  });


  // --- Step 4: 업로드 페이지에서 최종 CSRF 토큰 추출 ---
  let uploadCsrfToken = excelPageRes.html('meta[name=_csrf]').attr('content');

  if (!uploadCsrfToken) {
    console.error(`VU ${__VU}: 업로드 페이지에서 CSRF 토큰을 찾지 못했습니다.`);
    return;
  }

  // --- Step 5: 파일 업로드 POST 요청 ---
  const payload = {
    _csrf: uploadCsrfToken,
    file: http.file(uploadFile, 'test-completed-course-file.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'),
  };

  let uploadRes = http.post(
      'http://localhost:8080/excel/read',
      payload,
      {
        tags: { name: 'Excel_Upload_Request' },
        jar: jar,
      }
  );

  // --- Step 6: 최종 결과 검증 ---
  const uploadCheckResult = check(uploadRes, {
    '업로드 성공 (status 200)': (r) => r.status === 200,
  });

  // 업로드 파일 응답 결과 확인용 디버깅 코드
  // console.error(`VU ${__VU}: 업로드 응답 객체 전체:\n${JSON.stringify(uploadRes, null, 2)}`);

  sleep(1);
}
