/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.metadata;

import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Encapsulates information about an {@link Executable} ({@link Method} or {@link Constructor}) obtained through
 * introspection.
 *
 * @author oge
 * @since 14/06/2016
 */
@ToString
public class ExecutableInformation<E extends Executable> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutableInformation.class);

    private final E executable;
    private final List<ParameterInformation> parameters;

    ExecutableInformation(E executable) {
        this.executable = executable;
        this.parameters = parseParameters(executable);
    }

    private List<ParameterInformation> parseParameters(E executable) {
        LOGGER.debug("parsing parameters of: " + executable);
        List<ParameterInformation> list = new ArrayList<>();
        for (Parameter parameter : executable.getParameters()) {
            list.add(new ParameterInformation(parameter));
        }
        return list;
    }

    Set<Class<?>> getReferencedClasses() {
        ClassSet referencedClasses = new ClassSet();
        // add return type and its parameter types
//        if (executable instanceof Method) {
//            final Method method = (Method) this.executable;
//            referencedClasses.add(method.getReturnType());
//            referencedClasses.addParameterized(method.getGenericReturnType());
//        }
        // add parameters and their parameter types
        Collections.addAll(referencedClasses, executable.getParameterTypes());
        for (Type t : executable.getGenericParameterTypes()) {
            referencedClasses.addParameterized(t);
        }
        return referencedClasses;
    }

    public boolean isGetterOrSetter() {
        String methodName = executable.getName();
        final int paramCount = executable.getParameterCount();
        return ((methodName.startsWith("get") || methodName.startsWith("is")) && paramCount == 0)
                || (methodName.startsWith("set") && paramCount == 1);
    }

    public E getExecutable() {
        return executable;
    }

    public String getName() {
        return executable.getName();
    }

    public <C> boolean references(Class<C> targetClass) {
        return getReferencedClasses().contains(targetClass);
    }
}
