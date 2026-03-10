/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.introspection;

import com.ogerardin.guarana.core.annotations.Service;
import com.ogerardin.guarana.core.metamodel.ClassInformation;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Utility class for Java class introspection using reflection.
 * Provides methods to analyze class structure, extract type information,
 * and manage cached {@link ClassInformation} instances.
 *
 * @author Olivier Gérardin
 * @since 1.0
 */
@Slf4j
public class JavaIntrospector {

    private static final Map<Class, ClassInformation> classInformationByClass = new HashMap<>();

    /**
     * Given a method assumed to return a parameterized type with a single parameter type, returns the single parameter
     * type as declared by the method.
     * E.g. if method is List<Date> getDates(), the result will be Date.class
     */
    public static <C> Class<C> getMethodResultSingleParameterType(Method method) {
        final Type genericReturnType = method.getGenericReturnType();
        return getSingleParameterType(genericReturnType);
    }

    /**
     * Extracts the single type parameter from a parameterized type.
     *
     * @throws RuntimeException if the type is not a parameterized type
     */
    public static <C> Class<C> getSingleParameterType(Type genericType) {
        if (!(genericType instanceof ParameterizedType)) {
            throw new RuntimeException("Type is not a parameterized type: " + genericType);
        }
        final Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
        // We assume that the type has exactly one actual type parameter
        return (Class<C>) actualTypeArguments[0];
    }

    /**
     * Checks if the specified class is annotated with {@link Service}.
     */
    static boolean isService(Class<?> targetClass) {
        return Arrays.stream(targetClass.getAnnotations())
                .anyMatch(a -> a.getClass() == Service.class);
    }

    /**
     * Determines if a class is a system class (part of Java core libraries).
     * Returns true for primitive types and classes in java.*, javax.*, or sun.* packages.
     */
    public static boolean isSystem(Class targetClass) {
        if (targetClass.isPrimitive()) {
            return true;
        }
        final String targetClassName = targetClass.getName();
        return targetClassName.startsWith("java.")
                || targetClassName.startsWith("javax.")
                || targetClassName.startsWith("sun.");
    }

    /**
     * Checks if the specified class is a primitive type.
     */
    static boolean isPrimitive(Class clazz) {
        return clazz.isPrimitive();
    }

    /**
     * Returns the cached {@link ClassInformation} for the specified class,
     * creating and caching it if necessary.
     */
    public static <T> ClassInformation<T> getClassInformation(Class<T> clazz) {
        //noinspection unchecked
        ClassInformation<T> classInformation = classInformationByClass.get(clazz);
        if (classInformation != null) {
            return classInformation;
        }

        log.debug("Instantiating ClassInformation for: " + clazz);
        JavaClassIntrospector<T> introspector = new JavaClassIntrospector<>(clazz);
        classInformation = new ClassInformation<T>(clazz, introspector);
        classInformationByClass.put(clazz, classInformation);
        return classInformation;
    }

    /**
     * Checks if the specified executable references the target class in its signature.
     */
    static <C> boolean executableReferences(Executable executable, Class<C> targetClass) {
        return getReferencedClasses(executable).contains(targetClass);
    }

    private static Set<Class<?>> getReferencedClasses(Executable executable) {
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
}
