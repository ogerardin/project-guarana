/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.core.introspection;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Encapsulates information about a class obtained through introspection.
 *
 * @author oge
 * @since 14/06/2016
 */
public class ClassInformation<C> {

    private final Class<C> targetClass;

    private BeanInfo beanInfo;
    private List<MethodInformation> methods = null;
    private List<PropertyInformation> properties = null;

    ClassInformation(Class<C> targetClass) throws IntrospectionException {
        this.targetClass = targetClass;
        introspect();
    }

    private void introspect() throws IntrospectionException {
        beanInfo = java.beans.Introspector.getBeanInfo(targetClass);
    }


    public String getSimpleClassName() {
        return beanInfo.getBeanDescriptor().getBeanClass().getSimpleName();
    }

    public String getDisplayName() {
        return beanInfo.getBeanDescriptor().getDisplayName();
    }

    public Class<C> getTargetClass() {
        return targetClass;
    }

    public List<MethodInformation> getMethods() {
        if (methods == null) {
            methods = Arrays.asList(beanInfo.getMethodDescriptors()).stream()
                    .map(MethodInformation::new)
                    .collect(Collectors.toList());
        }
        return methods;
    }

    public List<PropertyInformation> getProperties() {
        if (properties == null) {
            properties = Arrays.asList(beanInfo.getPropertyDescriptors()).stream()
                    .map(PropertyInformation::new)
                    .collect(Collectors.toList());
        }
        return properties;
    }

    public List<Constructor<?>> getDeclaredConstructors() {
        return Arrays.asList(targetClass.getDeclaredConstructors());
    }
}
