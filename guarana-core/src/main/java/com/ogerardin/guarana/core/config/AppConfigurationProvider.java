/*
 * Copyright (c) 2025 Olivier Gérardin
 */

package com.ogerardin.guarana.core.config;

/**
 * Service provider interface for application-specific configuration.
 * Applications should implement this interface and register it via META-INF/services
 * to provide their guarana.properties configuration file.
 *
 * @author Olivier Gérardin
 * @since 1.0
 */
public interface AppConfigurationProvider {
    
    /**
     * Returns the resource name of the application's properties file.
     * The resource will be loaded using the provider's class, allowing it to
     * be found across module boundaries in JPMS.
     *
     * @return the resource path (e.g., "/guarana.properties")
     */
    String getPropertiesResourceName();
}
