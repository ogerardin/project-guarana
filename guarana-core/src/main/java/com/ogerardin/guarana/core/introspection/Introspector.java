/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.introspection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author oge
 * @since 07/09/2015
 */
public class Introspector {
    private static Logger LOGGER = LoggerFactory.getLogger(Introspector.class);

    private Introspector() {
    }

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

}
