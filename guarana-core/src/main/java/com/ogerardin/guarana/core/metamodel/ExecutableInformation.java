/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.metamodel;

import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.MethodDescriptor;
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
public class ExecutableInformation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutableInformation.class);

    private final Executable executable;
    private final List<ParameterInformation> parameters;

    public ExecutableInformation(Executable executable) {
        this.executable = executable;
        this.parameters = parseParameters(executable);
    }

    public ExecutableInformation(MethodDescriptor methodDescriptor) {
        this(methodDescriptor.getMethod());
    }

    private List<ParameterInformation> parseParameters(Executable executable) {
//        LOGGER.debug("parsing parameters of: " + executable);
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

    public Executable getExecutable() {
        return executable;
    }

    public String getName() {
        return executable.getName();
    }

    public <C> boolean references(Class<C> targetClass) {
        return getReferencedClasses().contains(targetClass);
    }

    public boolean isMethod() {
        return executable instanceof Method;
    }

    public boolean isConstructor() {
        return executable instanceof Constructor;
    }

    public String getDefaultLabel() {
        if (isMethod()) {
            return getDefaultLabel((Method) executable);
        } else {
            return getDefaultLabel((Constructor) executable);
        }
    }

    private static String getDefaultLabel(Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getReturnType().getSimpleName())
                .append(' ').append(method.getDeclaringClass().getSimpleName())
                .append('.').append(method.getName());
        appendParameters(method, sb);
        return sb.toString();
    }

    private static String getDefaultLabel(Constructor constructor) {
        StringBuilder sb = new StringBuilder();
        sb.append(constructor.getDeclaringClass().getSimpleName());
        appendParameters(constructor, sb);
        return sb.toString();
    }

    private static void appendParameters(Executable method, StringBuilder sb) {
        sb.append('(');
        final Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; ++i) {
            sb.append(parameterTypes[i].getSimpleName());
            if (i < parameterTypes.length - 1) {
                sb.append(",");
            }
        }
        sb.append(')');
    }


}
