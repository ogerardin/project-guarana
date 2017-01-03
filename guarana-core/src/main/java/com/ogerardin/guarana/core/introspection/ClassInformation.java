/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.core.introspection;

import com.ogerardin.guarana.core.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.annotation.AnnotationSupport;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.lang.annotation.Annotation;
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
    private final Set<Executable> relatedExecutables = new HashSet<>();


    private ClassInformation(Class<C> targetClass) throws IntrospectionException {
        this.targetClass = targetClass;
        this.beanInfo = java.beans.Introspector.getBeanInfo(this.targetClass);
        this.service = isService(targetClass);
    }

    private static boolean isService(Class<?> targetClass) {
        return Arrays.stream(targetClass.getAnnotations())
                .anyMatch(annotation -> annotation.getClass() == Service.class);
    }

    private void scanMethods() throws IntrospectionException {
        LOGGER.debug("Scanning methods of " + this.getTargetClass());

        for (ExecutableInformation ei : getMethodsAndConstructors()) {
            final Executable executable = ei.getExecutable();
            LOGGER.debug("executable: " + executable);

            // collect all types referenced in this method's declaration
            Set<Class> classes = ei.getReferencedClasses();
            LOGGER.debug("references classes: " + classes);

            // associate this method with each of the referenced classes
            classes.forEach(clazz -> addContributingExecutable(clazz, executable));
        }
    }

    private List<ExecutableInformation<?>> getMethodsAndConstructors() {
        return Stream.concat(getMethods().stream(), getConstructors().stream()).collect(Collectors.toList());
    }

    private static void addContributingExecutable(Class type, Executable executable) {
        LOGGER.debug("executable: " + executable);
        if (type.isArray()) {
            addContributingExecutable(type.getComponentType(), executable);
            return;
        }
        if (type.isPrimitive() || type.getPackage().getName().startsWith("java.")) {
            return;
        }

        try {
            ClassInformation returnTypeClassInfo = forClass(type);
            returnTypeClassInfo.addContributingExecutable(executable);
        } catch (IntrospectionException e) {
            LOGGER.warn("Failed to obtain class information for " + type);
        }
    }

    private void addContributingExecutable(Executable method) {
        LOGGER.debug("Add contributing " + method.getClass().getSimpleName()
                + " for " + this.getTargetClass().getSimpleName() + ": " + method);
//        LOGGER.debug("Add contributing executable for " + this.getTargetClass().getSimpleName() + " -> " +
//                method.getReturnType().getSimpleName() + " " +
//                method.getDeclaringClass().getSimpleName() + "." +
//                method.getName());
        relatedExecutables.add(method);
    }

    static <T> ClassInformation<T> forClass(Class<T> targetClass) throws IntrospectionException {
        LOGGER.debug("Getting class information for: " + targetClass);
        ClassInformation<T> classInformation = classInformationByClass.get(targetClass);
        if (classInformation != null) {
            LOGGER.debug("CACHE HIT: " + targetClass);
            return classInformation;
        }
        classInformation = new ClassInformation<T>(targetClass);
        classInformationByClass.put(targetClass, classInformation);
        classInformation.scanMethods();
        return classInformation;
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

    public List<ExecutableInformation<Method>> getMethods() {
        if (methods == null) {
            methods = Arrays.stream(beanInfo.getMethodDescriptors())
                    .map(MethodDescriptor::getMethod)
                    .map(ExecutableInformation::new)
                    .collect(Collectors.toList());
        }
        return methods;
    }

    public List<ExecutableInformation<Constructor>> getConstructors() {
        if (constructors == null) {
            constructors = Arrays.stream(beanInfo.getBeanDescriptor().getBeanClass().getConstructors())
                    .map(e -> new ExecutableInformation<Constructor>(e))
                    .collect(Collectors.toList());
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

    public Set<Executable> getRelatedExecutables() {
        return relatedExecutables;
    }

    public Set<Method> getRelatedMethods() {
        return relatedExecutables.stream()
                .filter(e -> e instanceof Method)
                .map(e -> (Method) e)
                .collect(Collectors.toSet());
    }

    public boolean isService() {
        return service;
    }

}
