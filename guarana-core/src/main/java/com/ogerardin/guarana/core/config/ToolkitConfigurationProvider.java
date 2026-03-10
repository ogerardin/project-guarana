/*
 * Copyright (c) 2024 Olivier Gérardin
 */

package com.ogerardin.guarana.core.config;

/**
 * Service provider interface for UI toolkit-specific configuration.
 * Implementations should provide the resource name for toolkit-specific properties.
 */
public interface ToolkitConfigurationProvider {
    
    /**
     * Returns the resource name (path) for the toolkit-specific properties file.
     * The resource should be on the classpath and start with "/".
     * 
     * @return the properties resource name, e.g., "/.guarana-ui.properties"
     */
    String getPropertiesResourceName();
}
