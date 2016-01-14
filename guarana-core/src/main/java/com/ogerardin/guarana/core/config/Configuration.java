/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.config;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author oge
 * @since 24/09/2015
 */
public class Configuration extends CompositeConfiguration {

    private final Map<Class, ClassConfiguration> classConfigurationMap = new HashMap<>();

    /**
     * Build default configuration by using system properties and properties from a file named "guarana.properties"
     */
    public Configuration() {
        try {
            addConfiguration(new SystemConfiguration());
            addConfiguration(new PropertiesConfiguration(getClass().getResource("/guarana.properties")));
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    public boolean getHumanizePropertyNames() {
        return getBoolean("humanizePropertyNames", true);
    }

    public boolean getHumanizeClassNames() {
        return getBoolean("humanizeClassNames", true);
    }

    public <C> ClassConfiguration<C> forClass(Class<C> clazz) {
        ClassConfiguration<C> classConfig = classConfigurationMap.get(clazz);
        if (classConfig == null) {
            classConfig = new ClassConfiguration<>(clazz);
            classConfigurationMap.put(clazz, classConfig);
        }
        return classConfig;
    }
}

