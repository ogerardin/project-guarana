/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.metamodel;

import com.ogerardin.guarana.core.introspection.JavaIntrospector;
import lombok.ToString;

import java.lang.reflect.Parameter;

/**
 * Encapsulates metadata about a method or constructor parameter,
 * including whether it should be injected as a service dependency.
 *
 * @author Olivier Gérardin
 * @since 1.0
 */
@ToString
public class ParameterInformation {

    private final Parameter parameter;
    private final boolean injected;

    /**
     * Creates a new ParameterInformation for the specified parameter,
     * automatically determining if it should be injected.
     */
    public ParameterInformation(Parameter parameter) {
        this.parameter = parameter;
        this.injected = isInjectable(parameter.getType());
    }

    /**
     * Determines if the specified type is eligible for dependency injection.
     * A type is injectable if it's not a system class and is annotated as a service.
     */
    private boolean isInjectable(Class<?> type) {
        if (JavaIntrospector.isSystem(type)) {
            return false;
        }
        ClassInformation<?> classInformation = JavaIntrospector.getClassInformation(type);
        return classInformation.isService();
    }
}
