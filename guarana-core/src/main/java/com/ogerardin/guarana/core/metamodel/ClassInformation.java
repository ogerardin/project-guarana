/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.metamodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Encapsulates information about a class obtained through introspection.
 *
 * @author oge
 * @since 14/06/2016
 */
public class ClassInformation<C> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassInformation.class);

    private final Class<C> javaClass;
    private final Introspector<C> introspector;

    private final String simpleClassName;
    private final String displayName;

    private List<ExecutableInformation> methods = null;
    private List<ExecutableInformation> constructors = null;

    private List<PropertyInformation> properties = null;


    public ClassInformation(Class<C> clazz, Introspector<C> introspector) {
        this.javaClass = clazz;
        this.introspector = introspector;
        this.simpleClassName = introspector.getSimpleName();
        this.displayName = introspector.getDisplayName();
    }

    public String getSimpleClassName() {
        return simpleClassName;
    }

    public String getBeanDisplayName() {
        return displayName;
    }

    public Class<C> getJavaClass() {
        return javaClass;
    }

    public List<ExecutableInformation> getMethods() {
        if (methods == null) {
            methods = introspector.getMethods().stream()
                    .map(ExecutableInformation::new)
                    .collect(Collectors.toList());
        }
        return methods;
    }

    public List<ExecutableInformation> getConstructors() {
        if (constructors == null) {
            //noinspection RedundantTypeArguments
            constructors = introspector.getConstructors().stream()
                    .map(ExecutableInformation::new)
                    .collect(Collectors.toList());
        }
        return constructors;
    }

    public List<PropertyInformation> getProperties() {
        if (properties == null) {
//            properties = introspector.getPropertyDescriptors().stream()
//                    .map(PropertyInformation::new)
//                    .collect(Collectors.toList());
            properties = introspector.getProperties();
        }
        return properties;
    }


    public boolean isService() {
        return introspector.isService();
    }
}
