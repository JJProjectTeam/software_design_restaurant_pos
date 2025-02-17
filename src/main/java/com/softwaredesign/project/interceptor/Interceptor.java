package com.softwaredesign.project.interceptor;

/**
 * Interceptor interface for handling inventory events
 */
public interface Interceptor {
    void intercept(InterceptorContext context);
}
