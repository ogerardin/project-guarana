/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.introspection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author oge
 * @since 07/09/2015
 */
public class Introspector {
    static Logger LOGGER = LoggerFactory.getLogger(Introspector.class);

    private static Map<Class, ClassInformation> classInfoMap = new HashMap<>();

    private Introspector() {
    }

    public static <C> ClassInformation<C> getClassInfo(Class<C> clazz) {
        ClassInformation<C> classInformation = classInfoMap.get(clazz);
        if (classInformation != null) {
            return classInformation;
        }
        try {
            classInformation = new ClassInformation<C>(clazz);
        } catch (IntrospectionException e) {
            LOGGER.error("Failed to introspect " + clazz, e);
            throw new RuntimeException(e);
        }
        classInfoMap.put(clazz, classInformation);
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
