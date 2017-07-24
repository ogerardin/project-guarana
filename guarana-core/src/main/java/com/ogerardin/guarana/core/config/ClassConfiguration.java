/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.config;

import com.ogerardin.guarana.core.ui.InstanceUI;
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

    private final Class<C> targetClass;

    private ToString<C> toString;
    private Class<? extends InstanceUI<C, ?>> uiClass;
    private Class<? extends InstanceUI<C, ?>> embeddedUiClass;

    private String displayName = null;
    private Boolean humanizePropertyNames = null;
    private final Set<String> hiddenProperties = new HashSet<>();
    private final Set<String> shownProperties = new HashSet<>();
    private final Set<Executable> hiddenMethods = new HashSet<>();
    private Boolean zoomable = null;


    public ClassConfiguration(Class<C> clazz) {
        targetClass = clazz;
    }

    public ClassConfiguration<C> hideProperties(String... propertyNames) {
        hiddenProperties.addAll(Arrays.asList(propertyNames));
        return this;
    }

    public boolean isHiddenProperty(String propertyName) {
        return hiddenProperties.contains(propertyName);
    }

    public ClassConfiguration<C> setToString(ToString<C> toString) {
        this.toString = toString;
        return this;
    }

    public String toString(C value) {
        if (value == null) {
            return "null";
        } else {
            return (toString != null) ? toString.toString(value) : value.toString();
        }
    }

    public Class<? extends InstanceUI<C, ?>> getUiClass() {
        return uiClass;
    }

    public Class<? extends InstanceUI<C, ?>> getEmbeddedUiClass() {
        return embeddedUiClass;
    }

    public <U extends InstanceUI<C, ?>> ClassConfiguration<C> setEmbeddedUiClass(Class<U> embeddedUiClass) {
        this.embeddedUiClass = embeddedUiClass;
        return this;
    }

    public <U extends InstanceUI<C, ?>> ClassConfiguration<C> setUiClass(Class<U> uiClass) {
        this.uiClass = uiClass;
        return this;
    }

    public void hideMethod(String methodName) {
        for (Method method : targetClass.getDeclaredMethods()) {
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
            if (method.getDeclaringClass() != this.targetClass) {
                throw new InvalidParameterException("Method " + method + " is not declared in class " + targetClass);
            }
//            LOGGER.debug("Hiding: "  + method.getDeclaringClass() + "." + method.getName());
            hiddenMethods.add(method);
        }
    }

    public void hideAllMethods() {
        hideMethods(targetClass.getDeclaredMethods());
    }

    public boolean isHidden(Executable method) {
//        Validate.isTrue(method.getDeclaringClass() == this.targetClass);
        return hiddenMethods.contains(method);
    }

    public void setHumanizePropertyNames(Boolean humanizePropertyNames) {
        this.humanizePropertyNames = humanizePropertyNames;
    }

    public Boolean isHumanizePropertyNames() {
        return humanizePropertyNames;
    }

    public void setEmbeddedUiClass(String className) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(className);
        if (!InstanceUI.class.isAssignableFrom(clazz)) {
            throw new ClassCastException("Class does not implement InstanceUI: " + className);
        }
        setEmbeddedUiClass((Class<? extends InstanceUI>) clazz);
    }

    public void setUiClass(String className) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(className);
        if (!InstanceUI.class.isAssignableFrom(clazz)) {
            throw new ClassCastException("Class does not implement InstanceUI: " + className);
        }
        setUiClass((Class<? extends InstanceUI>) clazz);
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
}
