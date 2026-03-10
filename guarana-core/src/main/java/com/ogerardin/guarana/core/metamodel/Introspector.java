/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.metamodel;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 * Interface for introspecting Java classes to extract metadata about their structure,
 * including methods, constructors, properties, and executables.
 *
 * @param <C> the type of class being introspected
 * @author Olivier Gérardin
 * @since 1.0
 */
public interface Introspector<C> {

    /**
     * Returns all public methods declared by the target class.
     */
    List<Method> getMethods();

    /**
     * Returns all public constructors of the target class.
     */
    List<Constructor<C>> getConstructors();

    /**
     * Returns all properties (getter/setter pairs) of the target class.
     */
    List<PropertyInformation> getProperties();

    /**
     * Returns all contributed executables (methods and constructors) that should be
     * exposed as UI actions.
     */
    Collection<Executable> getContributedExecutables();

    /**
     * Returns true if the target class is annotated as a service.
     */
    boolean isService();

    /**
     * Returns the simple name of the target class.
     */
    String getSimpleName();

    /**
     * Returns the display name of the target class for UI presentation.
     */
    String getDisplayName();
}
