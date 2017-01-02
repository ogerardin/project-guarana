/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.introspection;

import com.google.common.reflect.ClassPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Parameter;

/**
 * @author oge
 * @since 02/01/2017
 */
public class ParameterInformation {

    private static Logger LOGGER = LoggerFactory.getLogger(ParameterInformation.class);

    private final Parameter parameter;
    private final boolean injected;

    public ParameterInformation(Parameter parameter) {
        this.parameter = parameter;
        this.injected = isInjectable(parameter.getType());
    }

    private boolean isInjectable(Class<?> type) {
        ClassInformation classInformation = null;
        try {
            classInformation = ClassInformation.forClass(type);
        } catch (IntrospectionException e) {
            LOGGER.error("Failed to obtain class information for " + type, e);
            throw new RuntimeException(e);
        }
        return classInformation.isService();
    }
}
