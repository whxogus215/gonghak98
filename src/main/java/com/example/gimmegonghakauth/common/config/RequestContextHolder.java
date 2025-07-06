package com.example.gimmegonghakauth.common.config;

public class RequestContextHolder {

    // 각 요청 스레드별로 고유한 RequestContext를 갖도록 한다.
    private static ThreadLocal<RequestContext> CONTEXT = new ThreadLocal<>();

    public static void initContext(RequestContext context) {
        CONTEXT.remove();
        CONTEXT.set(context);
    }

    public static RequestContext getContext() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
