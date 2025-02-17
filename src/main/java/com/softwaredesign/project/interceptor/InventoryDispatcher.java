package com.softwaredesign.project.interceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete dispatcher implementation for inventory events
 */
public class InventoryDispatcher implements Dispatcher {
    private final List<Interceptor> interceptors;

    public InventoryDispatcher() {
        this.interceptors = new ArrayList<>();
    }

    @Override
    public void register(Interceptor interceptor) {
        interceptors.add(interceptor);
    }

    @Override
    public void remove(Interceptor interceptor) {
        interceptors.remove(interceptor);
    }

    @Override
    public void dispatch(InterceptorContext context) {
        for (Interceptor interceptor : interceptors) {
            interceptor.intercept(context);
            if (context.isGameOver()) {
                break;
            }
        }
    }
}
