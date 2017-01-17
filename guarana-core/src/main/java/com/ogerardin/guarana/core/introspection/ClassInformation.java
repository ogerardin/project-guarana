/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.core.introspection;

import com.ogerardin.guarana.core.annotations.Service;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Encapsulates information about a class obtained through introspection.
 *
 * @author oge
 * @since 14/06/2016
 */
public class ClassInformation<C> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassInformation.class);

    private static final Map<Class, ClassInformation> classInformationByClass = new HashMap<>();

    private final Class<C> targetClass;
    private final BeanInfo beanInfo;

    private final boolean service;

    private List<ExecutableInformation<Method>> methods = null;
    private List<ExecutableInformation<Constructor>> constructors = null;
    private List<PropertyInformation> properties = null;

    /**
     * Set of {@link Method}s that reference this class
     */
    private final Set<Executable> contributedExecutables = new HashSet<>();


    private ClassInformation(Class<C> targetClass) throws IntrospectionException {
        this.targetClass = targetClass;
        this.beanInfo = java.beans.Introspector.getBeanInfo(this.targetClass);
        this.service = isService(targetClass);
    }

    private static boolean isService(Class<?> targetClass) {
        return Arrays.stream(targetClass.getAnnotations())
                .anyMatch(annotation -> annotation.getClass() == Service.class);
    }

    static boolean isSystem(Class targetClass) {
        final String targetClassName = targetClass.getName();
        return targetClassName.startsWith("java.")
                || targetClassName.startsWith("javax.")
                || targetClassName.startsWith("sun.");
    }


    private void scanMethods() throws IntrospectionException {
        LOGGER.debug("Scanning methods/constructors of " + this.getTargetClass());

        for (ExecutableInformation ei : getMethodsAndConstructors()) {
            final Executable executable = ei.getExecutable();
            LOGGER.debug("scanning: " + executable);

            // collect all types referenced in this method's declaration
            Set<Class> classes = ei.getReferencedClasses();
//            LOGGER.debug("references classes: " + classes);

            // associate this method with each of the referenced classes
            for (Class c : classes) {
                addContributingExecutable(c, executable);
            }
        }
    }

    private List<ExecutableInformation<?>> getMethodsAndConstructors() throws IntrospectionException {
        return Stream.concat(getMethods().stream(), getConstructors().stream())
                .collect(Collectors.toList());
    }

    private static void addContributingExecutable(Class type, Executable executable) throws IntrospectionException {
        if (type.isPrimitive() || isSystem(type)) {
            return;
        }
        if (type.isArray()) {
            addContributingExecutable(type.getComponentType(), executable);
            return;
        }
        ClassInformation returnTypeClassInfo = forClass(type);
        returnTypeClassInfo.addContributingExecutable(executable);
    }

    private void addContributingExecutable(Executable method) {
        LOGGER.debug("Add contributing " + method.getClass().getSimpleName()
                + " for " + this.getTargetClass().getSimpleName() + ": " + method);
//        LOGGER.debug("Add contributing executable for " + this.getTargetClass().getSimpleName() + " -> " +
//                method.getReturnType().getSimpleName() + " " +
//                method.getDeclaringClass().getSimpleName() + "." +
//                method.getName());
        contributedExecutables.add(method);
    }

    public static <T> ClassInformation<T> forClass(Class<T> targetClass) throws IntrospectionException {
        ClassInformation<T> classInformation = classInformationByClass.get(targetClass);
        if (classInformation != null) {
            return classInformation;
        }

        LOGGER.debug("Getting class information for: " + targetClass);
        classInformation = new ClassInformation<T>(targetClass);
        classInformationByClass.put(targetClass, classInformation);
        if (!targetClass.isPrimitive() && !classInformation.isSystem()) {
            classInformation.scanMethods();
        }
        return classInformation;
    }

    private boolean isSystem() {
        return isSystem(getTargetClass());
    }

    public String getSimpleClassName() {
        return beanInfo.getBeanDescriptor().getBeanClass().getSimpleName();
    }

    public String getBeanDisplayName() {
        return beanInfo.getBeanDescriptor().getDisplayName();
    }

    public Class<C> getTargetClass() {
        return targetClass;
    }

    public List<ExecutableInformation<Method>> getMethods() throws IntrospectionException {
        if (methods == null) {
            methods = new ArrayList<>();
            final MethodDescriptor[] methodDescriptors = beanInfo.getMethodDescriptors();
            for (MethodDescriptor methodDescriptor : methodDescriptors) {
                Method method = methodDescriptor.getMethod();
                ExecutableInformation executableInformation = new ExecutableInformation<>(method);
                methods.add(executableInformation);
            }
        }
        return methods;
    }

    public List<ExecutableInformation<Constructor>> getConstructors() throws IntrospectionException {
        if (constructors == null) {
            constructors = new ArrayList<>();
            for (Constructor<?> e : beanInfo.getBeanDescriptor().getBeanClass().getConstructors()) {
                ExecutableInformation<Constructor> executableInformation = new ExecutableInformation<>(e);
                constructors.add(executableInformation);
            }
        }
        return constructors;
    }

    public List<PropertyInformation> getProperties() {
        if (properties == null) {
            properties = Arrays.stream(beanInfo.getPropertyDescriptors())
                    .map(PropertyInformation::new)
                    .collect(Collectors.toList());
        }
        return properties;
    }

    public List<Constructor> getDeclaredConstructors() {
        return Arrays.asList(targetClass.getDeclaredConstructors());
    }

    public Set<Executable> getContributedExecutables() {
        return contributedExecutables;
    }

    public Set<Method> getContributedMethods() {
        return contributedExecutables.stream()
                .filter(e -> e instanceof Method)
                .map(e -> (Method) e)
                .collect(Collectors.toSet());
    }

    public boolean isService() {
        return service;
    }

}
