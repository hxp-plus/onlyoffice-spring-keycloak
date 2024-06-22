package com.onlyoffice.integration.config;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingInterceptor extends HandlerInterceptorAdapter {
    private final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        String queryString = request.getQueryString();
        String remoteAddr = request.getRemoteAddr();
        logger.info("Received request from {} : {} {}{}", remoteAddr, method, path, (queryString != null ? "?" + queryString : ""));
        return true;
    }
}
