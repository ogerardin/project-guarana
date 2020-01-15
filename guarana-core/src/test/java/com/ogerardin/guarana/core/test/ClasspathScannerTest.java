/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.test;

import com.ogerardin.guarana.core.test.domain.Person;
import com.ogerardin.guarana.core.test.domain.Thing;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

/**
 * @author oge
 * @since 12/01/2017
 */
public class ClasspathScannerTest {

    @Test
    public void testClasspathScanning() {

        FastClasspathScanner fastClasspathScanner = new FastClasspathScanner();
        final List<String> namesOfAllStandardClasses = fastClasspathScanner.scan()
                .getNamesOfAllStandardClasses();

        assertThat(namesOfAllStandardClasses, hasItems(Person.class.getName(), Thing.class.getName()));
    }

    @Test
    public void testFindFieldWithType() throws Exception {
        FastClasspathScanner fastClasspathScanner = new FastClasspathScanner();
        fastClasspathScanner.enableFieldTypeIndexing();
        final List<String> namesOfClassesWithFieldOfType = fastClasspathScanner.scan()
                .getNamesOfClassesWithFieldOfType(Person.class);

        assertThat(namesOfClassesWithFieldOfType, hasItems(Thing.class.getName()));
    }
}
