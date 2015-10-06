/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.config;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;

/**
 * Created by oge on 24/09/2015.
 */
public class ConfigManager {

    static CompositeConfiguration config = new CompositeConfiguration();
    static {
        try {
            config.addConfiguration(new SystemConfiguration());
            config.addConfiguration(new PropertiesConfiguration("guarana.properties"));
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static String getProperty(String name) {
        return config.getString(name);
    }

    public static boolean getHumanizeProperties() {
        return config.getBoolean("humanizeProperties", true);
    }

}

