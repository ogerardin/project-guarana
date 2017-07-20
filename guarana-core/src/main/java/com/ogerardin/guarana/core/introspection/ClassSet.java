/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.introspection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;

/**
 * @author oge
 * @since 15/06/2016
 */
class ClassSet extends HashSet<Class<?>> {

    /**
     * Add the specified type's class and its parameters' classes recursively
     */
    void addParameterized(Type type) {
        if (type instanceof ParameterizedType) {
            for (Type t : ((ParameterizedType) type).getActualTypeArguments()) {
                addParameterized(t);
            }
        }
    }
}
