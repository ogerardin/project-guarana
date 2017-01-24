/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.introspection;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author oge
 * @since 07/09/2015
 */
public class Introspector<C> {

    private static Logger LOGGER = LoggerFactory.getLogger(Introspector.class);

    private final Class<C> targetClass;
    private final BeanInfo beanInfo;

    public Introspector(Class<C> targetClass) {
        this.targetClass = targetClass;
        try {
            this.beanInfo = java.beans.Introspector.getBeanInfo(targetClass);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

//    public static <C> ClassInformation<C> getClassInformation(Class<C> clazz) {
//        ClassInformation<C> classInformation = ClassInformation.forClass(clazz);
//        return classInformation;
//    }



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

    public Collection<Class<?>> getReferencingClasses() {
        final List<String> namesOfClassesWithFieldOfType = new FastClasspathScanner().scan()
                .getNamesOfClassesWithFieldOfType(targetClass);

        return namesOfClassesWithFieldOfType.stream()
                .map(c -> {
                    try {
                        return Class.forName(c);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet());

    }

    public Method[] getMethods() {
        return targetClass.getMethods();
    }

    public Constructor<?>[] getConstructors() {
        return targetClass.getConstructors();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        return beanInfo.getPropertyDescriptors();
    }

}
