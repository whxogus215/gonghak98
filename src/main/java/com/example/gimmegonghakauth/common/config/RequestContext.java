package com.example.gimmegonghakauth.common.config;

import com.example.gimmegonghakauth.common.constant.QueryType;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class RequestContext {

    private String httpMethod;
    private String bestMatchPattern;
    private Map<QueryType, Integer> queryCountByType = new HashMap<>();

    public RequestContext(String httpMethod, String bestMatchPattern) {
        this.httpMethod = httpMethod;
        this.bestMatchPattern = bestMatchPattern;
    }

    public void incrementQueryCount(String sql) {
        QueryType queryType = QueryType.from(sql);
        queryCountByType.merge(queryType, 1, Integer::sum);
    }
}
