/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.core;

/**
 * Main entry point for the Guarana framework. Provides access to framework
 * metadata and version information.
 *
 * @author Olivier Gérardin
 * @since 1.0
 */
public class Guarana {

    /**
     * Returns the implementation version of the Guarana framework.
     * Falls back to the specification version or "UNKNOWN" if not available.
     */
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
