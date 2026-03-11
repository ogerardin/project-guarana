/*
 * Copyright (c) 2025 Olivier Gérardin
 */

package com.ogerardin.guarana.demo.javafx.hr;

import com.ogerardin.guarana.core.config.AppConfigurationProvider;

/**
 * Configuration provider for the HR demo application.
 * Provides access to the application's guarana.properties file.
 *
 * @author Olivier Gérardin
 * @since 1.0
 */
public class DemoAppConfigurationProvider implements AppConfigurationProvider {
    
    @Override
    public String getPropertiesResourceName() {
        return "/guarana.properties";
    }
}
