/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.introspection;

import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

public interface Introspector {

    /**
     * Given a method assumed to return a parameterized type with a single parameter type, returns the single parameter
     * type as declared by the method.
     * E.g. if method is List<Date> getDates(), the result will be Date.class
     */
    static <C> Class<C> getMethodResultSingleParameterType(Method method) {
        final Type genericReturnType = method.getGenericReturnType();
        return getSingleParameterType(genericReturnType);
    }

    static <C> Class<C> getSingleParameterType(Type genericType) {
        if (!(genericType instanceof ParameterizedType)) {
            throw new RuntimeException("Type is not a parameterized type: " + genericType);
        }
        final Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
        // We assume that the type has exactly one actual type parameter
        return (Class<C>) actualTypeArguments[0];
    }

    Collection<Class<?>> getReferencingClasses();

    MethodDescriptor[] getMethods();

    Constructor<?>[] getConstructors();

    PropertyDescriptor[] getPropertyDescriptors();
}
