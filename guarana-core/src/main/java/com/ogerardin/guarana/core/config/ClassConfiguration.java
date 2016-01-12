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
    private Class uiClass;

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
        if (value == null) {
            return "null";
        } else {
            return (toString != null) ? toString.toString(value) : value.toString();
        }
    }

    public Class getUiClass() {
        return uiClass;
    }

    public <U extends InstanceUI> void setUiClass(Class<U> uiClass) {
        this.uiClass = uiClass;
    }
}
