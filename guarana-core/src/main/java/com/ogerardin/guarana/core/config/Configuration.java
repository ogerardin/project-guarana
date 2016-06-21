/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.config;

import com.ogerardin.guarana.core.introspection.ClassInformation;
import com.ogerardin.guarana.core.introspection.Introspector;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author oge
 * @since 24/09/2015
 */
public class Configuration extends CompositeConfiguration {
    private static Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    private static final String PROPERTY_PREFIX = "guarana.";

    private static final String CORE_PROPERTIES = "/_guarana_core.properties";
    private static final String TOOLKIT_PROPERTIES = "/_guarana_ui.properties";
    private static final String USER_PROPERTIES = "/guarana.properties";

    private final Map<Class, ClassConfiguration> classConfigurationMap = new HashMap<>();
    private boolean humanizeClassNames = false;

    /**
     * Build default configuration by using
     * -system properties
     * -core properties from _guarana_core.properties
     * -toolkit-specific properties from guarana_ui_default.properties
     * -user properties from guarana.properties
     */
    public Configuration() {
        // common defaults
        addConfiguration(new SystemConfiguration());
        try {
            addConfigurationResource(CORE_PROPERTIES);
        } catch (ConfigurationException e) {
            throw new RuntimeException("Failed to load " + CORE_PROPERTIES, e);
        }

        // toolkit-specific properties
        try {
            addConfigurationResource(TOOLKIT_PROPERTIES);
        } catch (ConfigurationException e) {
            throw new RuntimeException("Failed to load " + TOOLKIT_PROPERTIES +
                    " - you need one UI implementation in your classpath!", e);
        }

        // user-defined properties
        try {
            addConfigurationResource(USER_PROPERTIES);
        } catch (ConfigurationException e) {
            LOGGER.error("WARNING: no user configuration " + USER_PROPERTIES + " found");
        }

        applyConfiguration();
    }

    public static String humanize(String name) {
        // split "camelCase" to "camel" "Case"
        final String[] parts = name.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
        // fix case of each part and join into a space-separated string
        return Arrays.stream(parts)
                .map(part -> part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    private void addConfigurationResource(String resource) throws ConfigurationException {
        LOGGER.debug("reading configuration resource: " + resource);
        final URL url = getClass().getResource(resource);
        if (url == null) {
            throw new ConfigurationException("Resource not found: " + resource);
        }
        addConfiguration(new PropertiesConfiguration(url));
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
                    LOGGER.error("Configuration: invalid property key: " + key);
            }
        }
    }

    private void applyClassProperty(String className, String property, String key) {
        final ClassConfiguration<?> classConfiguration;
        try {
            Class<?> clazz = Class.forName(className);
            classConfiguration = this.forClass(clazz);
        } catch (ClassNotFoundException e) {
            LOGGER.error("ERROR: configuration: class not found: " + className);
            return;
        }
        switch (property) {
            case "hideMethods":
                classConfiguration.hideMethods(getStringArray(key));
                break;
            case "hideProperties":
                classConfiguration.hideProperties(getStringArray(key));
                break;
            case "showProperties":
                classConfiguration.showProperties(getStringArray(key));
                break;
            case "humanizePropertyNames":
                classConfiguration.setHumanizePropertyNames(getBoolean(key));
                break;
            case "displayName":
                classConfiguration.setDisplayName(getString(key));
                break;
            case "embeddedUiClass":
                try {
                    classConfiguration.setEmbeddedUiClass(getString(key));
                } catch (ClassNotFoundException e) {
                    LOGGER.error(e.toString());
                }
                break;
            default:
                LOGGER.error("Configuration: invalid class property: " + property);
        }
    }

    public boolean isHumanizeClassNames() {
        return humanizeClassNames;
    }

    public String getClassDisplayName(Class clazz) {
        // if class display name configured, use it
        final ClassConfiguration classConfiguration = forClass(clazz);
        String displayName = classConfiguration.getDisplayName();
        if (displayName != null) {
            return displayName;
        }

        // otherwise if bean display name is different from class name, use it
        final ClassInformation classInformation = Introspector.getClassInformation(clazz);
        final String className = classInformation.getSimpleClassName();
        String beanDisplayName = classInformation.getBeanDisplayName();
        if (!beanDisplayName.equals(className)) {
            return beanDisplayName;
        }

        // otherwise use class name (humanized if required)
        return isHumanizeClassNames() ? Configuration.humanize(className) : className;
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
     * Returns true if and only if the specified property is configured as shown for the specified class or any of
     * its superclasses.
     */
    public <C> boolean isShownProperty(Class<C> clazz, String property) {
        final Optional<Boolean> isHidden = findClassConfigurationRecursively(clazz, cc -> {
            if (cc.isShownProperty(property)) {
                return true;
            }
            else if (cc.isHiddenProperty(property)) {
                return false;
            }
            return null;
        });
        return isHidden.orElse(true);
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

