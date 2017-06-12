/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.config;

import com.ogerardin.guarana.core.metadata.ClassInformation;
import com.ogerardin.guarana.core.persistence.PersistenceServiceBuilder;
import com.ogerardin.guarana.core.persistence.basic.DefaultPersistenceServiceBuilder;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Configuration reader for Guarana.
 *
 * Configuration is read from the following resources:
 * {@code /.guarana-model.properties} contains default model properties
 * {@code /.guarana-ui.properties} contains UI toolkit specific properties
 * {@code /guarana.properties} contains user properties
 *
 * Each file contains a list of properties. Guarana properties are in one of the following forms:
 * {@code guarana.key=value} to set a global property
 * {@code guarana.class.fqcn.key=value} to set a property for a specific class (where fqcn is the fully qualified class name)
 *
 * @author oge
 * @since 24/09/2015
 */
public class Configuration extends CompositeConfiguration {
    private static Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    private static final String PROPERTY_PREFIX = "guarana.";

    private static final String CORE_PROPERTIES = "/.guarana-core.properties";
    private static final String TOOLKIT_PROPERTIES = "/.guarana-ui.properties";
    private static final String USER_PROPERTIES = "/guarana.properties";

    private final Map<Class, ClassConfiguration> classConfigurationByClass = new HashMap<>();
    private boolean humanizeClassNames = false;

    private Class<? extends PersistenceServiceBuilder> persistenceServiceBuilder = DefaultPersistenceServiceBuilder.class;

    /**
     * Build configuration by using default sources
     */
    public Configuration() {
        // priority 1: system properties
        addConfiguration(new SystemConfiguration());

        // priority 2: user-defined properties
        try {
            addConfigurationResource(USER_PROPERTIES);
        } catch (ConfigurationException e) {
            LOGGER.warn("Failed to load " + USER_PROPERTIES + ": " + e.getMessage());
        }

        // priority 3: toolkit-specific properties
        try {
            addConfigurationResource(TOOLKIT_PROPERTIES);
        } catch (ConfigurationException e) {
            throw new RuntimeException("Failed to load " + TOOLKIT_PROPERTIES +
                    " - you need one UI implementation in your classpath!", e);
        }

        // priority 4: Guarana model defaults
        try {
            addConfigurationResource(CORE_PROPERTIES);
        } catch (ConfigurationException e) {
            throw new RuntimeException("Failed to load " + CORE_PROPERTIES, e);
        }

        applyConfiguration();
    }

    private void addConfigurationResource(String resource) throws ConfigurationException {
        final URL url = getClass().getResource(resource);
        if (url == null) {
            throw new ConfigurationException("Resource not found: " + resource);
        }
        LOGGER.debug("Reading configuration resource: " + resource);
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
            String[] keyParts = guaranaKey.split("\\.");

            switch (keyParts[0]) {
                case "class":
                    String property = keyParts[keyParts.length - 1];
                    String className = guaranaKey.substring("class.".length(), guaranaKey.length() - property.length() - 1);
                    applyClassProperty(className, property, key);
                    break;
                default:
                    applyGlobalProperty(key);
            }
        }
    }

    private void applyGlobalProperty(String key) {
        String[] keyParts = key.split("\\.");
        switch (keyParts[1]) {
            case "humanizeClassNames":
                this.humanizeClassNames = getBoolean(key);
                break;
            case "defaultPersistenceServiceProvider":
                try {
                    this.setPersistenceServiceBuilderClass(getString(key));
                } catch (ClassNotFoundException e) {
                    LOGGER.error(e.toString());
                }
                break;
            default:
                LOGGER.error("Invalid property key: " + key);
        }
    }

    private void applyClassProperty(String className, String property, String key) {
        final ClassConfiguration<?> classConfiguration;
        try {
            Class<?> clazz = Class.forName(className);
            classConfiguration = this.forClass(clazz);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Class not found: " + className);
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
            case "zoomable":
                classConfiguration.setZoomable(getBoolean(key));
                break;
            case "embeddedUiClass":
                try {
                    classConfiguration.setEmbeddedUiClass(getString(key));
                } catch (ClassNotFoundException e) {
                    LOGGER.error(e.toString());
                }
                break;
            case "uiClass":
                try {
                    classConfiguration.setUiClass(getString(key));
                } catch (ClassNotFoundException e) {
                    LOGGER.error(e.toString());
                }
                break;
            default:
                LOGGER.error("Invalid class property: " + property + " in " + key);
        }
    }

    public boolean isHumanizeClassNames() {
        return humanizeClassNames;
    }

    public String getClassDisplayName(Class<?> clazz) {
        // if class display name configured, use it
        final ClassConfiguration classConfiguration = forClass(clazz);
        String displayName = classConfiguration.getDisplayName();
        if (displayName != null) {
            return displayName;
        }

        // otherwise if bean display name is different from class name, use it
        final ClassInformation classInformation = ClassInformation.forClass(clazz);
        final String className = classInformation.getSimpleClassName();
        String beanDisplayName = classInformation.getBeanDisplayName();
        if (!beanDisplayName.equals(className)) {
            return beanDisplayName;
        }

        // otherwise use class name (humanized if required)
        return isHumanizeClassNames() ? Util.humanize(className) : className;
    }


    /**
     * Retrieves the ClassConfiguration for the specified class. If it does not exist yet, creates
     * and return a default ClassConfiguration
     */
    public <C> ClassConfiguration<C> forClass(Class<C> clazz) {
        ClassConfiguration<C> classConfig = classConfigurationByClass.get(clazz);
        if (classConfig == null) {
            classConfig = new ClassConfiguration<>(clazz);
            classConfigurationByClass.put(clazz, classConfig);
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

    public <C> boolean isZoomable(Class<C> clazz) {
        if (clazz.isPrimitive()) {
            return false;
        }
        final Optional<Boolean> isZoomable = findClassConfigurationRecursively(clazz, ClassConfiguration::isZoomable);
        return isZoomable.orElse(true);
    }

    /**
     * Returns true if and only if the specified property is configured as shown for the specified class or any of
     * its superclasses.
     */
    public <C> boolean isShownProperty(Class<C> clazz, String property) {
        final Optional<Boolean> isShown = findClassConfigurationRecursively(clazz, cc -> isShownProperty(cc, property));
        return isShown.orElse(true);
    }

    private Boolean isShownProperty(ClassConfiguration<?> configuration, String property) {
        if (configuration.isShownProperty(property)) {
            return true;
        }
        if (configuration.isHiddenProperty(property)) {
            return false;
        }
        return null;
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
    private <C, R> Optional<R> findClassConfigurationRecursively(Class<C> clazz, Function<ClassConfiguration<?>, R> getter) {
        ClassConfiguration<C> classConfig = this.forClass(clazz);
        final R result = getter.apply(classConfig);
        if (result != null) {
            return Optional.of(result);
        }
        Class<?> parent = clazz.getSuperclass();
        return (parent == null) ? Optional.empty() : findClassConfigurationRecursively(parent, getter);
    }

    public void setPersistenceServiceBuilderClass(String persistenceServiceBuilderClass) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(persistenceServiceBuilderClass);
        if (!PersistenceServiceBuilder.class.isAssignableFrom(clazz)) {
            throw new ClassCastException("Class does not implement " + PersistenceServiceBuilder.class.getSimpleName() +
                    ": " + persistenceServiceBuilderClass);
        }
        setPersistenceServiceBuilderClass((Class<? extends PersistenceServiceBuilder>) clazz);
    }

    private void setPersistenceServiceBuilderClass(Class<? extends PersistenceServiceBuilder> clazz) {
        this.persistenceServiceBuilder = clazz;
    }
}

