/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.test;

import com.ogerardin.guarana.core.persistence.basic.BasicPersistenceService;
import com.ogerardin.guarana.domain1.model.Person;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

/**
 * Created by olivier on 18/05/2017.
 */
public class BasicPersistenceServiceTest {

    BasicPersistenceService<Person> persistenceService = new BasicPersistenceService<>(Person.class);

    @Test
    public void testSaveRead() throws IOException, ClassNotFoundException {
        Person p = new Person("bla");

        persistenceService.save(p);

        List<Person> all = persistenceService.getAll();

        assertThat(all, hasItems(p));

    }
}
