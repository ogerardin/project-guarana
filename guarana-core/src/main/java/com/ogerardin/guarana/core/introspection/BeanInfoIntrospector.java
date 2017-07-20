/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.introspection;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.ogerardin.guarana.core.util.LambdaExceptionUtil.rethrowFunction;

/**
 * @author oge
 * @since 07/09/2015
 */
public class BeanInfoIntrospector<C> implements Introspector {

    private static Logger LOGGER = LoggerFactory.getLogger(BeanInfoIntrospector.class);

    private final Class<C> clazz;
    private final BeanInfo beanInfo;

    public BeanInfoIntrospector(Class<C> clazz) {
        this.clazz = clazz;
        try {
            this.beanInfo = java.beans.Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

//    public static <C> ClassInformation<C> getClassInformation(Class<C> clazz) {
//        ClassInformation<C> classInformation = ClassInformation.forClass(clazz);
//        return classInformation;
//    }


    @Override
    public Collection<Class<?>> getReferencingClasses() {
        final List<String> namesOfClassesWithFieldOfType = new FastClasspathScanner().scan()
                .getNamesOfClassesWithFieldOfType(clazz);

        return namesOfClassesWithFieldOfType.stream()
                .map(rethrowFunction(Class::forName))
                .collect(Collectors.toSet());
    }

    @Override
    public MethodDescriptor[] getMethods() {
        return beanInfo.getMethodDescriptors();
    }

    @Override
    public Constructor<?>[] getConstructors() {
        return clazz.getConstructors();
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        return beanInfo.getPropertyDescriptors();
    }

}
