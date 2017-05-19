/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.test;

import com.ogerardin.guarana.domain1.model.Person;
import com.ogerardin.guarana.domain1.model.Thing;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author oge
 * @since 12/01/2017
 */
public class ClasspathScannerTest {

    @Test
    public void testClasspathScanning() {
        final List<String> namesOfAllStandardClasses = new FastClasspathScanner().scan()
                .getNamesOfAllStandardClasses();

        assertThat(namesOfAllStandardClasses, hasItems(Person.class.getName(), Thing.class.getName()));
    }

    @Test
    public void testFindFieldWithType() throws Exception {
        final List<String> namesOfClassesWithFieldOfType = new FastClasspathScanner().scan()
                .getNamesOfClassesWithFieldOfType(Person.class);

        Set<String> expected = new HashSet<String>() {
            {
                add(Thing.class.getName());
            }
        };
        final HashSet<String> actual = new HashSet<>(namesOfClassesWithFieldOfType);

        assertEquals(expected, actual);
    }
}
