/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author olivier
 * @since 07/11/2015.
 */
public class ClassConfiguration<C> {

    private final Set<String> hiddenProperties = new HashSet<>();
    private ToString<C> toString;

    public ClassConfiguration(Class<C> clazz) {
        hiddenProperties.add("class");
    }

    public ClassConfiguration hideProperties(String... propertyNames) {
        hiddenProperties.addAll(Arrays.asList(propertyNames));
        return this;
    }

    public boolean isHiddenProperty(String propertyName) {
        return hiddenProperties.contains(propertyName);
    }

    public void setToString(ToString<C> toString) {
        this.toString = toString;
    }

    public String toString(C value) {
        return (toString != null) ? toString.toString(value) : value.toString();
    }
}
