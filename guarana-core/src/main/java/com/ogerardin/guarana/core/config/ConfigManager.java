package com.ogerardin.guarana.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by oge on 24/09/2015.
 */
public class ConfigManager {

    private static Properties allproperties = new Properties();

    static {
        InputStream stream = ConfigManager.class.getClassLoader().getResourceAsStream("guarana.properties");
        try {
            allproperties.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getProperty(String name) {
        return allproperties.getProperty(name);
    }

    public static boolean getHumanizeProperties() {
        return Boolean.valueOf(getProperty("humanizeProperties"));
    }

}

