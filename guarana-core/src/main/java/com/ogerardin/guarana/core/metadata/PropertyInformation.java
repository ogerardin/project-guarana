/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.metadata;

import lombok.ToString;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * @author oge
 * @since 14/06/2016
 */
@ToString
public class PropertyInformation {

    private final PropertyDescriptor propertyDescriptor;

    PropertyInformation(PropertyDescriptor propertyDescriptor) {
        this.propertyDescriptor = propertyDescriptor;
    }

    public boolean isReadOnly() {
        return propertyDescriptor.getWriteMethod() == null;
    }

    public String getName() {
        return propertyDescriptor.getName();
    }

    public String getDisplayName() {
        return propertyDescriptor.getDisplayName();
    }

    public Class<?> getPropertyType() {
        return propertyDescriptor.getPropertyType();
    }

    public Method getReadMethod() {
        return propertyDescriptor.getReadMethod();
    }

    public Method getWriteMethod() {
        return propertyDescriptor.getWriteMethod();
    }

    public PropertyDescriptor getDescriptor() {
        return propertyDescriptor;
    }
}
