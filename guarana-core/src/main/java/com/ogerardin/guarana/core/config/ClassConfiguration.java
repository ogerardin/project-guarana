/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.config;

import com.ogerardin.guarana.core.ui.InstanceUI;

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
    private Class<?> uiClass;
    private Class<?> embeddedUiClass;


    public ClassConfiguration(Class<C> clazz) {
        hiddenProperties.add("class");
    }

    public ClassConfiguration<C> hideProperties(String... propertyNames) {
        hiddenProperties.addAll(Arrays.asList(propertyNames));
        return this;
    }

    public boolean isHiddenProperty(String propertyName) {
        return hiddenProperties.contains(propertyName);
    }

    public ClassConfiguration<C> setToString(ToString<C> toString) {
        this.toString = toString;
        return this;
    }

    public String toString(C value) {
        if (value == null) {
            return "null";
        } else {
            return (toString != null) ? toString.toString(value) : value.toString();
        }
    }

    public Class getUiClass() {
        return uiClass;
    }

    public Class getEmbeddedUiClass() {
        return embeddedUiClass;
    }

    public <U extends InstanceUI> ClassConfiguration<C> setEmbeddedUiClass(Class<U> embeddedUiClass) {
        this.embeddedUiClass = embeddedUiClass;
        return this;
    }

    public <U extends InstanceUI> ClassConfiguration<C> setUiClass(Class<U> uiClass) {
        this.uiClass = uiClass;
        return this;
    }
}
