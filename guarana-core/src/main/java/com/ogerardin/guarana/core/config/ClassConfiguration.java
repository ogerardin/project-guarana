/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.config;

import com.ogerardin.guarana.core.ui.InstanceUI;
import com.ogerardin.guarana.core.util.DefaultStringConverter;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * UI configuration for a specific class.
 *
 * @author olivier
 * @since 07/11/2015.
 */
public class ClassConfiguration<C> {
    private static Logger LOGGER = LoggerFactory.getLogger(ClassConfiguration.class);

    private final Class<C> clazz;

    private Class<? extends InstanceUI<?, C>> uiClass;
    private Class<? extends InstanceUI<?, C>> embeddedUiClass;

    private String displayName = null;
    private Boolean humanizePropertyNames = null;
    private final Set<String> hiddenProperties = new HashSet<>();
    private final Set<String> shownProperties = new HashSet<>();
    private final Set<Executable> hiddenMethods = new HashSet<>();
    private Boolean zoomable = null;

    private Class<? extends StringConverter<C>> stringConverterClass;
    private StringConverter<C> stringConverter;


    public ClassConfiguration(Class<C> clazz) {
        this.clazz = clazz;
    }

    public ClassConfiguration<C> hideProperties(String... propertyNames) {
        hiddenProperties.addAll(Arrays.asList(propertyNames));
        return this;
    }

    public boolean isHiddenProperty(String propertyName) {
        return hiddenProperties.contains(propertyName);
    }

    public String toString(C value) {
        if (value == null) {
            return "";
        } else {
            return value.toString();
        }
    }

    public Class<? extends InstanceUI<?, C>> getUiClass() {
        return uiClass;
    }

    public ClassConfiguration<C> setUiClass(Class<? extends InstanceUI<?, C>> uiClass) {
        this.uiClass = uiClass;
        return this;
    }

    public Class<? extends InstanceUI<?, C>> getEmbeddedUiClass() {
        return embeddedUiClass;
    }

    public ClassConfiguration<C> setEmbeddedUiClass(Class<? extends InstanceUI<?, C>> embeddedUiClass) {
        this.embeddedUiClass = embeddedUiClass;
        return this;
    }

    public void hideMethod(String methodName) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                hideMethods(method);
                return;
            }
        }
        LOGGER.warn("No method found matching " + methodName);
    }

    public void hideMethods(String... methods) {
        if (methods.length == 1 && methods[0].equals("*")) {
            hideAllMethods();
            return;
        }
        for (String method : methods) {
            hideMethod(method);
        }
    }

    private void hideMethods(Method... methods) {
        for (Method method : methods) {
            if (method.getDeclaringClass() != this.clazz) {
                throw new InvalidParameterException("Method " + method + " is not declared in class " + clazz);
            }
//            LOGGER.debug("Hiding: "  + method.getDeclaringClass() + "." + method.getName());
            hiddenMethods.add(method);
        }
    }

    public void hideAllMethods() {
        hideMethods(clazz.getDeclaredMethods());
    }

    public boolean isHidden(Executable method) {
//        Validate.isTrue(method.getDeclaringClass() == this.clazz);
        return hiddenMethods.contains(method);
    }

    public void setHumanizePropertyNames(Boolean humanizePropertyNames) {
        this.humanizePropertyNames = humanizePropertyNames;
    }

    public Boolean isHumanizePropertyNames() {
        return humanizePropertyNames;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isShownProperty(String property) {
        return shownProperties.contains(property);
    }

    public ClassConfiguration<C> showProperties(String... propertyNames) {
        shownProperties.addAll(Arrays.asList(propertyNames));
        return this;
    }

    public Boolean isZoomable() {
        return zoomable;
    }

    public void setZoomable(boolean zoomable) {
        this.zoomable = zoomable;
    }

    public StringConverter<C> getStringConverter() {
        if (stringConverter == null) {
            if (stringConverterClass != null) {
                try {
                    stringConverter = stringConverterClass.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } else {
                stringConverter = new DefaultStringConverter<>(clazz);
            }
        }
        return stringConverter;
    }

    public void setStringConverter(StringConverter<C> stringConverter) {
        this.stringConverter = stringConverter;
    }

    public void setStringConverterClass(Class<? extends StringConverter<C>> stringConverterClass) {
        this.stringConverterClass = stringConverterClass;
    }
}
