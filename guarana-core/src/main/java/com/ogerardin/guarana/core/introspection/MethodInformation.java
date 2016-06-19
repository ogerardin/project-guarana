/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.core.introspection;

import java.beans.MethodDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * @author oge
 * @since 14/06/2016
 */
public class MethodInformation {

    private final MethodDescriptor methodDescriptor;

    MethodInformation(MethodDescriptor methodDescriptor) {
        this.methodDescriptor = methodDescriptor;
    }

    Set<Class> getReferencedClasses() {
        ClassSet referencedClasses = new ClassSet();
        final Method method = methodDescriptor.getMethod();
        // add return type and its parameter types
        referencedClasses.add(method.getReturnType());
        referencedClasses.addParameterized(method.getGenericReturnType());
        // add parameters and their parameter types
        for (Class c : method.getParameterTypes()) {
            referencedClasses.add(c);
        }
        for (Type t : method.getGenericParameterTypes()) {
            referencedClasses.addParameterized(t);
        }
        return referencedClasses;
    }

    public boolean isGetterOrSetter() {
        String methodName = methodDescriptor.getName();
        final int paramCount = methodDescriptor.getMethod().getParameterCount();
        return ((methodName.startsWith("get") || methodName.startsWith("is")) && paramCount == 0)
                || (methodName.startsWith("set") && paramCount == 1);
    }

    public Method getMethod() {
        return methodDescriptor.getMethod();
    }

    public String getName() {
        return methodDescriptor.getName();
    }
}
