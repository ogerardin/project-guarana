/*
 * Copyright (c) 2024 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.config;

import com.ogerardin.guarana.core.config.ToolkitConfigurationProvider;

/**
 * JavaFX implementation of ToolkitConfigurationProvider.
 * Provides the resource name for JavaFX-specific configuration properties.
 */
public class JavaFxConfigurationProvider implements ToolkitConfigurationProvider {
    
    @Override
    public String getPropertiesResourceName() {
        return "/.guarana-ui.properties";
    }
}
