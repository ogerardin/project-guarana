/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.test;

import com.ogerardin.guarana.core.introspection.JavaIntrospector;
import com.ogerardin.guarana.core.metamodel.ClassInformation;
import com.ogerardin.guarana.core.metamodel.ExecutableInformation;
import com.ogerardin.guarana.core.test.domain.Person;
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
        ClassInformation<Person> classInformation = JavaIntrospector.getClassInformation(Person.class);

        // expected: constructors from native introspection
        final Set<Constructor> expected = Arrays.stream(Person.class.getConstructors())
                .collect(Collectors.toSet());

        // actual: constructors from ClassInformation
        final Set<Executable> actual = classInformation.getConstructors().stream()
                .map(ExecutableInformation::getExecutable)
                .collect(Collectors.toSet());

        Assert.assertEquals(expected, actual);
    }


}
