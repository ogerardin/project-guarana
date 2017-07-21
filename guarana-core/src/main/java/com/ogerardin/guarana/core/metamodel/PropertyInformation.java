/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.metamodel;

import lombok.ToString;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * @author oge
 * @since 14/06/2016
 */
@ToString
public class PropertyInformation {

    private final PropertyDescriptor propertyDescriptor;
    private final PropertyDescriptor jfxProperty;

    public PropertyInformation(PropertyDescriptor propertyDescriptor) {
        this(propertyDescriptor, null);
    }

    public PropertyInformation(PropertyDescriptor propertyDescriptor, PropertyDescriptor jfxProperty) {
        this.propertyDescriptor = propertyDescriptor;
        this.jfxProperty = jfxProperty;
    }

    public boolean isCollection() {
        return Collection.class.isAssignableFrom(getPropertyType());
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

    public PropertyDescriptor getJfxProperty() {
        return jfxProperty;
    }

    public PropertyDescriptor getPropertyDescriptor() {
        return propertyDescriptor;
    }

}
