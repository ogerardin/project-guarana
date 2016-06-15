/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.core.introspection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Encapsulates information about a class obtained through introspection.
 *
 * @author oge
 * @since 14/06/2016
 */
public class ClassInformation<C> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassInformation.class);

    private static final Map<Class, ClassInformation> classInfoMap = new HashMap<>();

    private final Class<C> targetClass;
    private final BeanInfo beanInfo;

    private List<MethodInformation> methods = null;
    private List<PropertyInformation> properties = null;

    /**
     * Set of {@link Method}s that reference this class
     */
    private final Set<Method> relatedMethods = new HashSet<>();


    private ClassInformation(Class<C> targetClass) throws IntrospectionException {
        this.targetClass = targetClass;
        beanInfo = java.beans.Introspector.getBeanInfo(this.targetClass);
    }

    private void scanMethods() throws IntrospectionException {
        LOGGER.debug("scanning " + this.getTargetClass());

        for (MethodInformation methodInformation : getMethods()) {
            final Method method = methodInformation.getMethod();

            // collect all types referenced in this method's declaration
            Set<Class> classes = methodInformation.getReferencedClasses();

            // associate this method with each of the referenced classes
            classes.forEach(clazz -> addReferencingMethod(clazz, method));
        }
    }

    private static void addReferencingMethod(Class type, Method method) {
        if (type.isArray()) {
            addReferencingMethod(type.getComponentType(), method);
            return;
        }
        if (type.isPrimitive() || type.getPackage().getName().startsWith("java.")) {
            return;
        }
        try {
            ClassInformation returnTypeClassInfo = forClass(type);
            returnTypeClassInfo.addReferencingMethod(method);
        } catch (IntrospectionException e) {
            LOGGER.warn("Failed to obtain class information for " + type);
        }
    }

    private void addReferencingMethod(Method method) {
        LOGGER.debug("PUT " + this.getTargetClass() + " -> " + method);
        relatedMethods.add(method);
    }

    static <T> ClassInformation<T> forClass(Class<T> targetClass) throws IntrospectionException {
        ClassInformation<T> classInformation = classInfoMap.get(targetClass);
        if (classInformation != null) {
            return classInformation;
        }
        classInformation = new ClassInformation<T>(targetClass);
        classInfoMap.put(targetClass, classInformation);
        classInformation.scanMethods();
        return classInformation;
    }


    public String getSimpleClassName() {
        return beanInfo.getBeanDescriptor().getBeanClass().getSimpleName();
    }

    public String getDisplayName() {
        return beanInfo.getBeanDescriptor().getDisplayName();
    }

    public Class<C> getTargetClass() {
        return targetClass;
    }

    public List<MethodInformation> getMethods() {
        if (methods == null) {
            methods = Arrays.asList(beanInfo.getMethodDescriptors()).stream()
                    .map(MethodInformation::new)
                    .collect(Collectors.toList());
        }
        return methods;
    }

    public List<PropertyInformation> getProperties() {
        if (properties == null) {
            properties = Arrays.asList(beanInfo.getPropertyDescriptors()).stream()
                    .map(PropertyInformation::new)
                    .collect(Collectors.toList());
        }
        return properties;
    }

    public List<Constructor<?>> getDeclaredConstructors() {
        return Arrays.asList(targetClass.getDeclaredConstructors());
    }

    public Set<Method> getRelatedMethods() {
        return relatedMethods;
    }


}
