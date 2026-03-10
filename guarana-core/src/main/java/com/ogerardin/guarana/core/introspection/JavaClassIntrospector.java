/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.introspection;

import com.ogerardin.guarana.core.metamodel.Introspector;
import com.ogerardin.guarana.core.metamodel.PropertyInformation;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import lombok.extern.slf4j.Slf4j;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ogerardin.guarana.core.util.LambdaExceptionUtil.rethrowFunction;

/**
 * Implementation of {@link Introspector} that uses Java reflection and
 * JavaBeans introspection to extract class metadata.
 *
 * @param <C> the type of class being introspected
 * @author Olivier Gérardin
 * @since 1.0
 */
@Slf4j
public class JavaClassIntrospector<C> implements Introspector<C> {

    private static final String JAVAFX_PROPERTY_SUFFIX = "Property";

    private final Class<C> clazz;
    private final BeanInfo beanInfo;

    private List<Executable> contributedExecutables = null;

    /**
     * Creates a new introspector for the specified class.
     */
    public JavaClassIntrospector(Class<C> clazz) {
        this.clazz = clazz;
        try {
            this.beanInfo = java.beans.Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
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

    /**
     * Returns true if the target class is a primitive type.
     */
    public boolean isPrimitive() {
        return JavaIntrospector.isPrimitive(clazz);
    }

    private void scanReferencing() {
        if (isPrimitive() || isSystem()) {
            return;
        }

        // examine classes referenced by this class
//        log.debug("Scanning methods/constructors of " + this.getJavaClass());
//        for (ExecutableInformation ei : getMethodsAndConstructors()) {
//            final Executable executable = ei.getExecutable();
//            log.debug("scanning: " + executable);
//
//            // collect all types referenced in this method's declaration
//            Set<Class<?>> classes = ei.getReferencedClasses();
//
//            classes.stream()
//                    .filter(c -> ! isSystem(c))
//                    .forEach(c -> addContributingExecutable(c, executable));
//        }

        // examine classes that reference this class
        log.debug("Scanning referencing classes for " + clazz);
        final Collection<Class> referencingClasses = getReferencingClasses();

        for (Class c : referencingClasses) {
            log.debug("" + c + " references " + clazz);
            final List<Executable> methodsAndConstructors = getMethodsAndConstructors(c);
            for (Executable executable : methodsAndConstructors) {
                if (JavaIntrospector.executableReferences(executable, clazz)) {
                    log.debug("" + executable + " references " + clazz);
                    addContributingExecutable(clazz, executable);
                }
            }
        }
    }

    private void addContributingExecutable(Class type, Executable executable) {
        if (type.isPrimitive() || JavaIntrospector.isSystem(type)) {
            return;
        }
        if (type.isArray()) {
            addContributingExecutable(type.getComponentType(), executable);
            return;
        }
        addContributingExecutable(executable);
    }

    private void addContributingExecutable(Executable executable) {
        log.debug("Add contributing " + executable.getClass().getSimpleName()
                + " for " + clazz.getSimpleName() + ": " + executable);
//        log.debug("Add contributing executable for " + this.getJavaClass().getSimpleName() + " -> " +
//                method.getReturnType().getSimpleName() + " " +
//                method.getDeclaringClass().getSimpleName() + "." +
//                method.getName());
        getContributedExecutables().add(executable);
    }

    //    public static <C> ClassInformation<C> getClassInformation(Class<C> clazz) {
//        ClassInformation<C> classInformation = ClassInformation.getClassInformation(clazz);
//        return classInformation;
//    }


    private Collection<Class> getReferencingClasses() {
        // getNamesOfClassesWithFieldOfType() also returns classes that have methods with paraneters of
        // the specified type, which fits our needs
        FastClasspathScanner fastClasspathScanner = new FastClasspathScanner();
        fastClasspathScanner.enableFieldTypeIndexing();
        final List<String> referencingClassNames = fastClasspathScanner.scan()
                .getNamesOfClassesWithFieldOfType(clazz);

        return referencingClassNames.stream()
                .map(rethrowFunction(Class::forName))
                .collect(Collectors.toSet());
    }

    @Override
    /**
     * Returns all public methods declared by the target class.
     */
    public List<Method> getMethods() {
        return Arrays.stream(beanInfo.getMethodDescriptors()).map(MethodDescriptor::getMethod).collect(Collectors.toList());
    }

    @Override
    /**
     * Returns all public constructors of the target class.
     */
    public List<Constructor<C>> getConstructors() {
        @SuppressWarnings("unchecked")
        Constructor<C>[] constructors = (Constructor<C>[]) clazz.getConstructors();
        return Arrays.asList(constructors);
    }

    @Override
    /**
     * Returns all properties (getter/setter pairs) of the target class.
     * JavaFX properties are paired with their corresponding bean properties.
     */
    public List<PropertyInformation> getProperties() {

        List<PropertyDescriptor> propertyDescriptors = Arrays.asList(beanInfo.getPropertyDescriptors());

        // create a map by property name
        Map<String, PropertyDescriptor> propertyDescriptorByName =
                propertyDescriptors.stream()
                        .collect(Collectors.toMap(
                                PropertyDescriptor::getName,
                                Function.identity(),
                                (a, b) -> b));

        // group basic property with corresponding JavaFX Property if any
        List<PropertyInformation> properties =
                propertyDescriptors.stream()
                .filter(pd -> !pd.getName().endsWith(JAVAFX_PROPERTY_SUFFIX))
                .map(pd -> {
                    String name = pd.getName();
                    PropertyDescriptor jfxProperty = propertyDescriptorByName.get(name + JAVAFX_PROPERTY_SUFFIX);
                    return new PropertyInformation(pd, jfxProperty);
                })
                .collect(Collectors.toList());

        //TODO order properties using declared fields order
        Field[] fields = clazz.getFields();


        return properties;
    }

    @Override
    /**
     * Returns all contributed executables (methods and constructors) that should be
     * exposed as UI actions. This includes methods from other classes that reference
     * this class in their signatures.
     */
    public Collection<Executable> getContributedExecutables() {
        if (contributedExecutables != null) {
            return contributedExecutables;
        }
        contributedExecutables = new ArrayList<>();
        scanReferencing();
        return contributedExecutables;
    }

    @Override
    /**
     * Returns true if the target class is annotated as a service.
     */
    public boolean isService() {
        return JavaIntrospector.isService(clazz);
    }

    @Override
    /**
     * Returns the simple name of the target class.
     */
    public String getSimpleName() {
        return beanInfo.getBeanDescriptor().getBeanClass().getSimpleName();
    }

    @Override
    /**
     * Returns the display name of the target class for UI presentation.
     */
    public String getDisplayName() {
        return beanInfo.getBeanDescriptor().getDisplayName();
    }
}
