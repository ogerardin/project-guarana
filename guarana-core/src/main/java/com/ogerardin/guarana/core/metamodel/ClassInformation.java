/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.metamodel;

import com.ogerardin.guarana.core.annotations.Service;
import com.ogerardin.guarana.core.introspection.BeanInfoIntrospector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
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
    private final BeanInfoIntrospector<C> introspector;

    private final boolean isService;
    private final String simpleClassName;
    private final String displayName;

    private List<ExecutableInformation> methods = null;
    private List<ExecutableInformation> constructors = null;

    private List<ExecutableInformation> contributedExecutables = null;

    private List<PropertyInformation> properties = null;


    private ClassInformation(Class<C> targetClass) {
        this.targetClass = targetClass;
        this.introspector = new BeanInfoIntrospector<>(targetClass);
        try {
            BeanInfo beanInfo = java.beans.Introspector.getBeanInfo(this.targetClass);
            this.simpleClassName = beanInfo.getBeanDescriptor().getBeanClass().getSimpleName();
            this.displayName = beanInfo.getBeanDescriptor().getDisplayName();
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
        this.isService = isService(targetClass);
    }

    private static boolean isService(Class<?> targetClass) {
        return Arrays.stream(targetClass.getAnnotations())
                .anyMatch(a -> a.getClass() == Service.class);
    }

    static boolean isSystem(Class targetClass) {
        if (targetClass.isPrimitive()) {
            return true;
        }
        final String targetClassName = targetClass.getName();
        return targetClassName.startsWith("java.")
                || targetClassName.startsWith("javax.")
                || targetClassName.startsWith("sun.");
    }

    private void scanReferencing() {
        if (isPrimitive() || isSystem()) {
            return;
        }

        // examine classes referenced by this class
//        LOGGER.debug("Scanning methods/constructors of " + this.getTargetClass());
//        for (ExecutableInformation ei : getMethodsAndConstructors()) {
//            final Executable executable = ei.getExecutable();
//            LOGGER.debug("scanning: " + executable);
//
//            // collect all types referenced in this method's declaration
//            Set<Class<?>> classes = ei.getReferencedClasses();
//
//            classes.stream()
//                    .filter(c -> ! isSystem(c))
//                    .forEach(c -> addContributingExecutable(c, executable));
//        }

        // examine classes that reference this class
        LOGGER.debug("Scanning referencing classes for " + this.getTargetClass());
        final Collection<Class<?>> referencingClasses = introspector.getReferencingClasses();

        for (Class c : referencingClasses) {
            LOGGER.debug("" + c + " references " + targetClass);
            ClassInformation classInformation = ClassInformation.forClass(c);
            final List<ExecutableInformation> methodsAndConstructors = classInformation.getMethodsAndConstructors();
            for (ExecutableInformation ei : methodsAndConstructors) {
                if (ei.references(targetClass)) {
                    LOGGER.debug("" + ei + " references " + targetClass);
                    addContributingExecutable(targetClass, ei);
                }
            }
        }
    }

    public boolean isPrimitive() {
        return targetClass.isPrimitive();
    }

    private List<ExecutableInformation> getMethodsAndConstructors() {
        return Stream.concat(getMethods().stream(), getConstructors().stream())
                .collect(Collectors.toList());
    }

    private static void addContributingExecutable(Class type, ExecutableInformation executable) {
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

    private void addContributingExecutable(ExecutableInformation executableInformation) {
        LOGGER.debug("Add contributing " + executableInformation.getClass().getSimpleName()
                + " for " + this.getTargetClass().getSimpleName() + ": " + executableInformation);
//        LOGGER.debug("Add contributing executable for " + this.getTargetClass().getSimpleName() + " -> " +
//                method.getReturnType().getSimpleName() + " " +
//                method.getDeclaringClass().getSimpleName() + "." +
//                method.getName());
        getContributedExecutables().add(executableInformation);
    }

    public static <T> ClassInformation<T> forClass(Class<T> targetClass) {
        ClassInformation<T> classInformation = classInformationByClass.get(targetClass);
        if (classInformation != null) {
            return classInformation;
        }

        LOGGER.debug("Getting class information for: " + targetClass);
        classInformation = new ClassInformation<>(targetClass);
        classInformationByClass.put(targetClass, classInformation);
        return classInformation;
    }

    private boolean isSystem() {
        return isSystem(getTargetClass());
    }

    public String getSimpleClassName() {
        return simpleClassName;
    }

    public String getBeanDisplayName() {
        return displayName;
    }

    public Class<C> getTargetClass() {
        return targetClass;
    }

    public List<ExecutableInformation> getMethods() {
        if (methods == null) {
            methods = Arrays.stream(introspector.getMethods())
                    .map(ExecutableInformation::new)
                    .collect(Collectors.toList());
        }
        return methods;
    }

    public List<ExecutableInformation> getConstructors() {
        if (constructors == null) {
            //noinspection RedundantTypeArguments
            constructors = Arrays.stream(introspector.getConstructors())
                    .map(ExecutableInformation::new)
                    .collect(Collectors.toList());
        }
        return constructors;
    }

    public List<PropertyInformation> getProperties() {
        if (properties == null) {
            properties = Arrays.stream(introspector.getPropertyDescriptors())
                    .map(PropertyInformation::new)
                    .collect(Collectors.toList());
        }
        return properties;
    }

    public Collection<ExecutableInformation> getContributedExecutables() {
        if (contributedExecutables != null) {
            return contributedExecutables;
        }
        contributedExecutables = new ArrayList<>();
        scanReferencing();
        return contributedExecutables;
    }

    public boolean isService() {
        return isService;
    }

}
