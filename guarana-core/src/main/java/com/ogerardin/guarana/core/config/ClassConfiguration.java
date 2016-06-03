/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.config;

import com.ogerardin.guarana.core.ui.InstanceUI;

import java.lang.reflect.Method;
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

    private final Class<C> targetClass;

    private ToString<C> toString;
    private Class<?> uiClass;
    private Class<?> embeddedUiClass;

    private Boolean humanizePropertyNames = null;
    private final Set<String> hiddenProperties = new HashSet<>();
    private final Set<Method> hiddenMethods = new HashSet<>();


    public ClassConfiguration(Class<C> clazz) {
        targetClass = clazz;
//        hiddenProperties.add("class");
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

    public Class getUiClass() {
        return uiClass;
    }

    public Class getEmbeddedUiClass() {
        return embeddedUiClass;
    }

    public <U extends InstanceUI> ClassConfiguration<C> setEmbeddedUiClass(Class<U> embeddedUiClass) {
        this.embeddedUiClass = embeddedUiClass;
        return this;
    }

    public <U extends InstanceUI> ClassConfiguration<C> setUiClass(Class<U> uiClass) {
        this.uiClass = uiClass;
        return this;
    }

    public void hideMethod(String methodName) {
        for (Method method : targetClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                hiddenMethods.add(method);
                System.out.println("DEBUG: hidden method: " + method);
            }
        }
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

    public void hideMethods(Method... methods) {
        for (Method method : methods) {
            hiddenMethods.add(method);
            System.out.println("DEBUG: hidden method " + method);
        }
    }

    public void hideAllMethods() {
        hideMethods(targetClass.getDeclaredMethods());
    }

    public boolean isHiddenMethod(Method method) {
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
            throw new ClassCastException("EmbeddedUiClass must implement InstanceUI: " + className);
        }
        setEmbeddedUiClass((Class<? extends InstanceUI>) clazz);
    }
}
