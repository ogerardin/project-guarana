/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.test;

import com.ogerardin.guarana.core.metadata.ClassInformation;
import com.ogerardin.guarana.test.model.Person;
import com.ogerardin.guarana.test.model.Thing;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author oge
 * @since 12/01/2017
 */
public class ClassInformationTest {

    @Test
    public void testContributed() throws Exception {
        ClassInformation<Person> classInformation = ClassInformation.forClass(Person.class);

        final Set<Constructor<?>> constructors = Arrays.stream(Thing.class.getConstructors())
                .filter(constructor -> Arrays.asList(constructor.getParameterTypes()).contains(Person.class))
                .collect(Collectors.toSet());

        classInformation.getContributedExecutables().forEach(System.out::println);
//        classInformation.getMethods().forEach(System.out::println);

        Assert.assertTrue(classInformation.getContributedExecutables().containsAll(constructors));

    }
}