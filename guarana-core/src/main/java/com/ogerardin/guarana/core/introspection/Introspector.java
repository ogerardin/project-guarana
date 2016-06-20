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
import java.util.Arrays;
import java.util.stream.Collectors;

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

    public static String humanize(String name) {
        // split "camelCase" to "camel" "Case"
        final String[] parts = name.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
        // fix case of each part and join into a space-separated string
        return Arrays.stream(parts)
                .map(part -> part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    /**
     * Given a method assumed to return a parameterized type with a single parameter type, returns the single paramater
     * type as declared by the method.
     */
    public static <C> Class<C> getCollectionItemType(Method readMethod) {
        final Type genericReturnType = readMethod.getGenericReturnType();
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
