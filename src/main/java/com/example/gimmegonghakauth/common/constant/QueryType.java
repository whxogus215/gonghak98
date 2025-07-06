package com.example.gimmegonghakauth.common.constant;

public enum QueryType {
    SELECT,
    INSERT,
    UPDATE,
    DELETE,
    UNKNOWN;

    public static QueryType from(String sql) {
        if (sql == null || sql.isBlank()) {
            return UNKNOWN;
        }

        String upperCaseSql = sql.trim().toUpperCase();

        if (upperCaseSql.startsWith(SELECT.name())) {
            return SELECT;
        }
        if (upperCaseSql.startsWith(INSERT.name())) {
            return INSERT;
        }
        if (upperCaseSql.startsWith(UPDATE.name())) {
            return UPDATE;
        }
        if (upperCaseSql.startsWith(DELETE.name())) {
            return DELETE;
        }
        return UNKNOWN;
    }
}
