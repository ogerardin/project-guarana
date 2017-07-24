/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.metamodel;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface Introspector<C> {

    List<Method> getMethods();

    List<Constructor<C>> getConstructors();

    Set<PropertyInformation> getProperties();

    Collection<Executable> getContributedExecutables();

    boolean isService();

    String getSimpleName();

    String getDisplayName();
}
