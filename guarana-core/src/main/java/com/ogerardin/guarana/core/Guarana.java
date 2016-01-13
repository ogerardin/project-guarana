/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.core;

/**
 * @author oge
 * @since 13/01/2016
 */
public class Guarana {

    public static String getImplementationVersion() {
        String version = null;
        Package pkg = Guarana.class.getPackage();
        if (pkg != null) {
            version = pkg.getImplementationVersion();
            if (version == null) {
                version = pkg.getSpecificationVersion();
            }
        }
        return (version != null) ? version : "UNKNOWN";
    }

}
