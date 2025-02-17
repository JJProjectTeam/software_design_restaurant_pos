package com.softwaredesign.project.interceptor;

/**
 * Dispatcher for the interceptor pattern
 */
public interface Dispatcher {
    void register(Interceptor interceptor);
    void remove(Interceptor interceptor);
    void dispatch(InterceptorContext context);
}
