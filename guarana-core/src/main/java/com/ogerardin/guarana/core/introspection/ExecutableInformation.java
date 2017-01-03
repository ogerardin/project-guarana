/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.core.introspection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Encapsulates information about an {@link Executable} ({@link Method} or {@link Constructor}) obtained through
 * introspection.
 *
 * @author oge
 * @since 14/06/2016
 */
public class ExecutableInformation<E extends Executable> {

    private static final Logger LOGGER =  LoggerFactory.getLogger(ExecutableInformation.class);;

    private final E executable;
    private final List<ParameterInformation> parameters;

    ExecutableInformation(E executable) {
        this.executable = executable;
        this.parameters = parseParameters(executable);
    }

    private List<ParameterInformation> parseParameters(E executable) {
        LOGGER.debug("parsing parameters of: " + executable);
        return Arrays.asList(executable.getParameters()).stream()
                .map(ParameterInformation::new)
                .collect(Collectors.toList());
    }

    Set<Class> getReferencedClasses() {
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
}
