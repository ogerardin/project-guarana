/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.metamodel;

import com.ogerardin.guarana.core.introspection.JavaIntrospector;
import lombok.ToString;

import java.lang.reflect.Parameter;

/**
 * @author oge
 * @since 02/01/2017
 */
@ToString
public class ParameterInformation {

    private final Parameter parameter;
    private final boolean injected;

    public ParameterInformation(Parameter parameter) {
        this.parameter = parameter;
        this.injected = isInjectable(parameter.getType());
    }

    private boolean isInjectable(Class<?> type) {
        if (JavaIntrospector.isSystem(type)) {
            return false;
        }
        ClassInformation<?> classInformation = JavaIntrospector.getClassInformation(type);
        return classInformation.isService();
    }
}
