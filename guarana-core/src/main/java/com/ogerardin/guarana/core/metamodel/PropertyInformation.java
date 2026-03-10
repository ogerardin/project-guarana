/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.metamodel;

import lombok.ToString;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Encapsulates metadata about a JavaBean property, including its type,
 * read/write methods, and optional JavaFX property mapping.
 *
 * @author Olivier Gérardin
 * @since 1.0
 */
@ToString
public class PropertyInformation {

    private final PropertyDescriptor propertyDescriptor;
    private final PropertyDescriptor jfxProperty;

    /**
     * Creates a new PropertyInformation for a standard JavaBean property.
     */
    public PropertyInformation(PropertyDescriptor propertyDescriptor) {
        this(propertyDescriptor, null);
    }

    /**
     * Creates a new PropertyInformation with an associated JavaFX property.
     */
    public PropertyInformation(PropertyDescriptor propertyDescriptor, PropertyDescriptor jfxProperty) {
        this.propertyDescriptor = propertyDescriptor;
        this.jfxProperty = jfxProperty;
    }

    /**
     * Returns true if the property type is a Collection or one of its subtypes.
     */
    public boolean isCollection() {
        return Collection.class.isAssignableFrom(getPropertyType());
    }

    /**
     * Returns true if the property has no setter method (read-only).
     */
    public boolean isReadOnly() {
        return propertyDescriptor.getWriteMethod() == null;
    }

    /**
     * Returns the property name.
     */
    public String getName() {
        return propertyDescriptor.getName();
    }

    /**
     * Returns the display name of the property for UI presentation.
     */
    public String getDisplayName() {
        return propertyDescriptor.getDisplayName();
    }

    /**
     * Returns the type of the property.
     */
    public Class<?> getPropertyType() {
        return propertyDescriptor.getPropertyType();
    }

    /**
     * Returns the getter method for this property.
     */
    public Method getReadMethod() {
        return propertyDescriptor.getReadMethod();
    }

    /**
     * Returns the setter method for this property, or null if read-only.
     */
    public Method getWriteMethod() {
        return propertyDescriptor.getWriteMethod();
    }

    /**
     * Returns the associated JavaFX property descriptor, if any.
     */
    public PropertyDescriptor getJfxProperty() {
        return jfxProperty;
    }

    /**
     * Returns the underlying JavaBeans property descriptor.
     */
    public PropertyDescriptor getPropertyDescriptor() {
        return propertyDescriptor;
    }

}
