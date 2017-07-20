/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.test;

import com.ogerardin.guarana.core.introspection.JavaClassIntrospector;
import com.ogerardin.guarana.core.test.domain.Person;
import com.ogerardin.guarana.core.test.domain.Thing;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class JavaClassIntrospectorTest {

    @Test
    public void testContributedConstructor() throws Exception {

        JavaClassIntrospector<Person> personIntrospector = new JavaClassIntrospector<>(Person.class);

        // expected: Thing constructors that take a Person as param
        final Set<Constructor> expected = Arrays.stream(Thing.class.getConstructors())
                .filter(c -> Arrays.asList(c.getParameterTypes()).contains(Person.class))
                .collect(Collectors.toSet());

        // actual: contributed executables for Person that are Thing constructors
        final Set<Executable> actual = personIntrospector.getContributedExecutables().stream()
                .filter(e -> e.getDeclaringClass() == Thing.class)
                .filter(e -> e instanceof Constructor)
                .collect(Collectors.toSet());

        Assert.assertEquals(expected, actual);

    }

}
