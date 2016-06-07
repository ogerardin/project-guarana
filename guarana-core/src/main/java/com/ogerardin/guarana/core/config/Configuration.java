/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.config;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author oge
 * @since 24/09/2015
 */
public class Configuration extends CompositeConfiguration {

    public static final String PROPERTY_PREFIX = "guarana.";

    private final Map<Class, ClassConfiguration> classConfigurationMap = new HashMap<>();
    private boolean humanizeClassNames = false;

    /**
     * Build default configuration by using system properties and properties from a file named "guarana.properties"
     */
    public Configuration() {
        try {
            addConfiguration(new SystemConfiguration());
            addConfiguration(new PropertiesConfiguration(getClass().getResource("/guarana_default.properties")));
            addConfiguration(new PropertiesConfiguration(getClass().getResource("/guarana.properties")));
            applyConfiguration();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parse the relevant properties and set up the configuration
     */
    private void applyConfiguration() {
        for (String key : (Iterable<String>) this::getKeys) {
            if (!key.startsWith(PROPERTY_PREFIX)) {
                //ignore other keys, since we also have system properties that we don't care about
                continue;
            }

            String guaranaKey = key.substring(PROPERTY_PREFIX.length());
            final String[] keyParts = guaranaKey.split("\\.");
            String property = keyParts[keyParts.length - 1];

            switch (keyParts[0]) {
                case "class":
                    String className = guaranaKey.substring("class.".length(), guaranaKey.length() - property.length() - 1);
                    applyClassProperty(className, property, key);
                    break;
                case "humanizeClassNames":
                    this.humanizeClassNames = getBoolean(key);
                    break;
                default:
                    System.err.println("ERROR: configuration: invalid property key: " + key);
            }
        }
    }

    private void applyClassProperty(String className, String property, String key) {
        final ClassConfiguration<?> classConfiguration;
        try {
            Class<?> clazz = Class.forName(className);
            classConfiguration = this.forClass(clazz);
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: configuration: class not found: " + className);
            return;
        }
        switch (property) {
            case "hideMethods":
                classConfiguration.hideMethods(getStringArray(key));
                break;
            case "hideProperties":
                classConfiguration.hideProperties(getStringArray(key));
                break;
            case "humanizePropertyNames":
                classConfiguration.setHumanizePropertyNames(getBoolean(key));
                break;
            case "embeddedUiClass":
                try {
                    classConfiguration.setEmbeddedUiClass(getString(key));
                } catch (ClassNotFoundException e) {
                    System.err.println(e.toString());
                }
                break;
            default:
                System.err.println("ERROR: configuration: invalid class property: " + property);
        }
    }

    public boolean getHumanizeClassNames() {
        return humanizeClassNames;
    }

    /**
     * Retrieves the ClassConfiguration for the specified class. If it does not exist yet, creates
     * and return a default ClassConfiguration
     */
    public <C> ClassConfiguration<C> forClass(Class<C> clazz) {
        ClassConfiguration<C> classConfig = classConfigurationMap.get(clazz);
        if (classConfig == null) {
            classConfig = new ClassConfiguration<>(clazz);
            classConfigurationMap.put(clazz, classConfig);
        }
        return classConfig;
    }

    /**
     * Returns true if and only if the specified method is configured as hidden for the specified class or any of
     * its superclasses.
     */
    public <C> boolean isHiddenMethod(Class<C> clazz, Method method) {
        final Optional<Boolean> isHidden = findClassConfigurationRecursively(clazz, cc -> cc.isHiddenMethod(method) ? true : null);
        return isHidden.orElse(false);
    }

    /**
     * Returns true if and only if the specified property is configured as hidden for the specified class or any of
     * its superclasses.
     */
    public <C> boolean isHiddenProperty(Class<C> clazz, String property) {
        final Optional<Boolean> isHidden = findClassConfigurationRecursively(clazz, cc -> cc.isHiddenProperty(property) ? true : null);
        return isHidden.orElse(false);
    }

    /**
     * Returns the value of property "humanizePropertyNames" as configured for the lowest-level class
     * in the hierarchy of the specified class, or false by default.
     */
    public <C> boolean isHumanizePropertyNames(Class<C> clazz) {
        final Optional<Boolean> isHumanize = findClassConfigurationRecursively(clazz, ClassConfiguration::isHumanizePropertyNames);
        return isHumanize.orElse(false);
    }

    /**
     * Find the first non-null result of applying the specified getter on the class configuration matching the specified class
     * or its superclasses in ascending order.
     *
     * @param clazz  The start class
     * @param getter The function to apply to the {@link ClassConfiguration}
     * @return A non-empty {@link Optional} containing the function's result, or {@link Optional#empty()} if the function
     * never returned a non-null result.
     */
    private <R> Optional<R> findClassConfigurationRecursively(Class<?> clazz, Function<ClassConfiguration<?>, R> getter) {
        ClassConfiguration<?> classConfig = this.forClass(clazz);
        final R result = getter.apply(classConfig);
        if (result != null) {
            return Optional.of(result);
        }
        Class parent = clazz.getSuperclass();
        if (parent == null) {
            return Optional.empty();
        }
        return findClassConfigurationRecursively(parent, getter);
    }

}

