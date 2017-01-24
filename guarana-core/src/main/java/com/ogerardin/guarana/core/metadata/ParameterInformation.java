/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.metadata;

import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Parameter;

/**
 * @author oge
 * @since 02/01/2017
 */
@ToString
public class ParameterInformation {

    private static Logger LOGGER = LoggerFactory.getLogger(ParameterInformation.class);

    private final Parameter parameter;
    private final boolean injected;

    public ParameterInformation(Parameter parameter) {
        this.parameter = parameter;
        this.injected = isInjectable(parameter.getType());
    }

    private boolean isInjectable(Class<?> type) {
        if (ClassInformation.isSystem(type)) {
            return false;
        }
        ClassInformation<?> classInformation = ClassInformation.forClass(type);
        return classInformation.isService();
    }
}
