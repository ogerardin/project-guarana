/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.introspection;

import com.ogerardin.guarana.core.metamodel.ClassInformation;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ogerardin.guarana.core.util.LambdaExceptionUtil.rethrowFunction;

/**
 * @author oge
 * @since 07/09/2015
 */
public class JavaClassIntrospector<C> {
    private static Logger LOGGER = LoggerFactory.getLogger(JavaClassIntrospector.class);

    private final Class<C> clazz;
    private final BeanInfo beanInfo;

    private List<Executable> contributedExecutables = null;

    public JavaClassIntrospector(Class<C> clazz) {
        this.clazz = clazz;
        try {
            this.beanInfo = java.beans.Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isPrimitive() {
        return clazz.isPrimitive();
    }

    private static List<Executable> getMethodsAndConstructors(Class c) {
        return Stream.concat(
                Arrays.stream(c.getMethods()),
                Arrays.stream(c.getConstructors()))
                .collect(Collectors.toList());
    }

    private boolean isSystem() {
        return JavaIntrospector.isSystem(clazz);
    }

    private void scanReferencing() {
        if (isPrimitive() || isSystem()) {
            return;
        }

        // examine classes referenced by this class
//        LOGGER.debug("Scanning methods/constructors of " + this.getJavaClass());
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
        LOGGER.debug("Scanning referencing classes for " + clazz);
        final Collection<Class> referencingClasses = getReferencingClasses();

        for (Class c : referencingClasses) {
            LOGGER.debug("" + c + " references " + clazz);
            final List<Executable> methodsAndConstructors = getMethodsAndConstructors(c);
            for (Executable executable : methodsAndConstructors) {
                if (JavaIntrospector.executableReferences(executable, clazz)) {
                    LOGGER.debug("" + executable + " references " + clazz);
                    addContributingExecutable(clazz, executable);
                }
            }
        }
    }

    public void addContributingExecutable(Class type, Executable executable) {
        if (type.isPrimitive() || JavaIntrospector.isSystem(type)) {
            return;
        }
        if (type.isArray()) {
            addContributingExecutable(type.getComponentType(), executable);
            return;
        }
        ClassInformation returnTypeClassInfo = JavaIntrospector.getClassInformation(type);
        addContributingExecutable(executable);
    }

    private void addContributingExecutable(Executable executable) {
        LOGGER.debug("Add contributing " + executable.getClass().getSimpleName()
                + " for " + clazz.getSimpleName() + ": " + executable);
//        LOGGER.debug("Add contributing executable for " + this.getJavaClass().getSimpleName() + " -> " +
//                method.getReturnType().getSimpleName() + " " +
//                method.getDeclaringClass().getSimpleName() + "." +
//                method.getName());
        getContributedExecutables().add(executable);
    }

    //    public static <C> ClassInformation<C> getClassInformation(Class<C> clazz) {
//        ClassInformation<C> classInformation = ClassInformation.getClassInformation(clazz);
//        return classInformation;
//    }


    public Collection<Class> getReferencingClasses() {
        // getNamesOfClassesWithFieldOfType also retruns classes that have methods with paraneters of
        // the specified type, which fits our needs
        final List<String> referencingClassNames = new FastClasspathScanner().scan()
                .getNamesOfClassesWithFieldOfType(clazz);

        return referencingClassNames.stream()
                .map(rethrowFunction(Class::forName))
                .collect(Collectors.toSet());
    }

    public List<Method> getMethods() {
        return Arrays.stream(beanInfo.getMethodDescriptors()).map(MethodDescriptor::getMethod).collect(Collectors.toList());
    }

    public List<Constructor<C>> getConstructors() {
        Constructor<C>[] constructors = (Constructor<C>[]) clazz.getConstructors();
        return Arrays.asList(constructors);
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        return beanInfo.getPropertyDescriptors();
    }

    public Collection<Executable> getContributedExecutables() {
        if (contributedExecutables != null) {
            return contributedExecutables;
        }
        contributedExecutables = new ArrayList<>();
        scanReferencing();
        return contributedExecutables;
    }

    public boolean isService() {
        return JavaIntrospector.isService(clazz);
    }

    public String getSimpleName() {
        return beanInfo.getBeanDescriptor().getBeanClass().getSimpleName();
    }

    public String getDisplayName() {
        return beanInfo.getBeanDescriptor().getDisplayName();
    }
}
