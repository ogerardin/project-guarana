/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.introspection;

import java.lang.reflect.Method;

/**
 * Created by oge on 24/09/2015.
 */
public class MethodCall {
    private final Method method;

    public MethodCall(Method method) {
        this.method = method;
    }
}
