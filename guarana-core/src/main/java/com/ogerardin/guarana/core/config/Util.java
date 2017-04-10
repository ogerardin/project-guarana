/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.config;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author oge
 * @since 07/03/2017
 */
public enum Util {
    ;

    public static String humanize(String name) {
        // split "camelCase" to "camel" "Case"
        final String[] parts = name.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
        // fix case of each part and join into a space-separated string
        return Arrays.stream(parts)
                .map(part -> part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}
