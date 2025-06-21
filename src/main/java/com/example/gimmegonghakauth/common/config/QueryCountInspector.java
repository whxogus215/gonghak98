package com.example.gimmegonghakauth.common.config;

import org.hibernate.resource.jdbc.spi.StatementInspector;

public class QueryCountInspector implements StatementInspector {

    @Override
    public String inspect(String sql) {
        RequestContext context = RequestContextHolder.getContext();
        if (context != null) {
            context.incrementQueryCount(sql);
        }
        return sql;
    }
}
