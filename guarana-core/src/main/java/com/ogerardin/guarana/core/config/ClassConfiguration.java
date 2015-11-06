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
public class ClassConfiguration {

    private final Set<String> hiddenProperties = new HashSet<>();

    public ClassConfiguration(Class<?> clazz) {
    }

    public ClassConfiguration hideProperties(String... propertyNames) {
        hiddenProperties.addAll(Arrays.asList(propertyNames));
        return this;
    }

    public boolean isHiddenProperty(String propertyName) {
        return hiddenProperties.contains(propertyName);
    }
}
