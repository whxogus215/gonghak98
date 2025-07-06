package com.example.gimmegonghakauth.common.config;

import com.example.gimmegonghakauth.common.constant.QueryType;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueryCountInterceptor implements HandlerInterceptor {

    private final MeterRegistry meterRegistry;

    /*
     * 스프링 인터셉터로 특정 컨트롤러에 들어오는 요청을 가로채서 "HTTP 메서드", "요청 경로"를 추출한다.
     * */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String httpMethod = request.getMethod();
        String bestMatchPattern = (String) request.getAttribute(
            HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

        RequestContext context = new RequestContext(httpMethod, bestMatchPattern);
        RequestContextHolder.initContext(context);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        /* 누적된 쿼리 횟수를 Metric으로 저장해 프로메테우스에 전달
         * */
        RequestContext context = RequestContextHolder.getContext();
        Map<QueryType, Integer> queryCountByType = context.getQueryCountByType();
        queryCountByType.forEach(((queryType, count) -> increment(context, queryType, count)));

        RequestContextHolder.clear();

    }

    // 프로메테우스에게 저장된 지표 값을 전달 -> 프로메테우스는 전달받은 지표 값을 그라파나에게 전달 -> 그라파는 전달받은 지표를 시각화
    private void increment(RequestContext context, QueryType queryType, Integer queryCount) {
        DistributionSummary summary = DistributionSummary.builder("app.query.per_request")
            .description("Number of SQL queries per request")
            .tag("path", context.getBestMatchPattern())
            .tag("http_method", context.getHttpMethod())
            .tag("query_type", queryType.name())
            .publishPercentiles(0.5, 0.95)
            .register(meterRegistry);
        log.info("url : {}, sql : {}, count : {}", context.getBestMatchPattern(), queryType.name(), queryCount);

        summary.record(queryCount);
    }
}
