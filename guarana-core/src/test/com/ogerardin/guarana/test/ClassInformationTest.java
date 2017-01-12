/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.test;

import com.ogerardin.guarana.core.introspection.ClassInformation;
import com.ogerardin.guarana.test.model.Person;
import org.junit.Test;

/**
 * @author oge
 * @since 12/01/2017
 */
public class ClassInformationTest {

    @Test
    public void testContributed() throws Exception {
        ClassInformation<Person> classInformation = ClassInformation.forClass(Person.class);

        classInformation.getContributedMethods().forEach(System.out::println);
    }
}