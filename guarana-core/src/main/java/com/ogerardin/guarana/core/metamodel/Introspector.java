/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.metamodel;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

public interface Introspector<C> {

    List<Method> getMethods();

    List<Constructor<C>> getConstructors();

    List<PropertyInformation> getProperties();

    Collection<Executable> getContributedExecutables();

    boolean isService();

    String getSimpleName();

    String getDisplayName();
}
