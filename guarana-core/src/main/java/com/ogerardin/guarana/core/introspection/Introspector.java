/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.introspection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author oge
 * @since 07/09/2015
 */
public enum Introspector {

    ; // instance-less enum, provides only static methods

    private static Logger LOGGER = LoggerFactory.getLogger(Introspector.class);

    public static <C> ClassInformation<C> getClassInformation(Class<C> clazz) {
        ClassInformation<C> classInformation;
        try {
            classInformation = ClassInformation.forClass(clazz);
        } catch (IntrospectionException e) {
            LOGGER.error("Failed to obtain class information for " + clazz, e);
            throw new RuntimeException(e);
        }
        return classInformation;
    }

    /**
     * Given a method assumed to return a parameterized type with a single parameter type, returns the single parameter
     * type as declared by the method.
     * E.g. if method is List<Date> getDates(), the result will be Date.class
     */
    public static <C> Class<C> getMethodResultSingleParameterType(Method method) {
        final Type genericReturnType = method.getGenericReturnType();
        return getSingleParameterType(genericReturnType);
    }

    public static <C> Class<C> getSingleParameterType(Type genericType) {
        if (!(genericType instanceof ParameterizedType)) {
            throw new RuntimeException("Type is not a parameterized type: " + genericType);
        }
        final Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
        // We assume that the type has exactly one actual type parameter
        return (Class<C>) actualTypeArguments[0];
    }

}
