/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.test;

import com.ogerardin.business.sample.thing.model.Person;
import com.ogerardin.business.sample.thing.model.Thing;
import com.ogerardin.guarana.core.metadata.ClassInformation;
import com.ogerardin.guarana.core.metadata.ExecutableInformation;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author oge
 * @since 12/01/2017
 */
public class ClassInformationTest {

    @Test
    public void testConstructors() {
        ClassInformation<Person> classInformation = ClassInformation.forClass(Person.class);

        // expected: constructors for Employee from native introspection
        final Set<Constructor> expected = Arrays.stream(Person.class.getConstructors())
                .collect(Collectors.toSet());

        // actual: constructors for Employee from ClassInformation
        final Set<Constructor> actual = classInformation.getConstructors().stream()
                .map(ExecutableInformation::getExecutable)
                .collect(Collectors.toSet());

        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testContributedConstructor() throws Exception {
        ClassInformation<Person> classInformation = ClassInformation.forClass(Person.class);

        // expected: Thing constructors that take a Employee as param
        final Set<Constructor<?>> expected = Arrays.stream(Thing.class.getConstructors())
                .filter(c -> Arrays.asList(c.getParameterTypes()).contains(Person.class))
                .collect(Collectors.toSet());

        // actual: contributed executables for Employee that are Thing constructors
        final Set<Executable> actual = classInformation.getContributedExecutables().stream()
                .map(ExecutableInformation::getExecutable)
                .filter(e -> e.getDeclaringClass() == Thing.class)
                .filter(e -> e instanceof Constructor)
                .collect(Collectors.toSet());

        Assert.assertEquals(expected, actual);

    }
}
