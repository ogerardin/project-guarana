/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.util;

import javafx.util.StringConverter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultStringConverter<T> extends StringConverter<T> {

    private final List<String> FACTORY_METHODS = Arrays.asList("of", "valueOf");

    private  Set<Method> methods;
    private  Constructor<T> constructor;

    public DefaultStringConverter(Class<T> clazz) {
        try {
            // try to get a constructor that takes a String as single argument
            constructor = clazz.getConstructor(String.class);
            methods = null;
        } catch (NoSuchMethodException e) {
            constructor = null;
            // build a list of factory methods that take a String as single argument
            methods = Arrays.stream(clazz.getMethods())
                    .filter(m -> Modifier.isStatic(m.getModifiers()))
                    .filter(m -> FACTORY_METHODS.contains(m.getName()))
                    .filter(m -> m.getReturnType() == clazz)
                    .filter(m -> m.getParameterCount() == 1 && m.getParameterTypes()[0] == String.class)
                    .collect(Collectors.toSet());
        }
    }

    @Override
    public String toString(T object) {
        return object == null ? "" : object.toString();
    }

    @Override
    public T fromString(String string) {
        if (constructor != null) {
            try {
                return constructor.newInstance(string);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                // log something
            }
            return null;
        }

        for (Method method : methods) {
            try {
                //noinspection unchecked
                return (T) method.invoke(null, string);
            } catch (IllegalAccessException | InvocationTargetException e) {
                // log something ?
            }
        }
        return null;
    }
}
