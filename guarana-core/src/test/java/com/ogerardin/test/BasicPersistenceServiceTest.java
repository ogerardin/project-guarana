/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.test;

import com.ogerardin.business.sample.thing.model.Person;
import com.ogerardin.guarana.core.persistence.basic.BasicPersistenceService;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

/**
 * Created by olivier on 18/05/2017.
 */
public class BasicPersistenceServiceTest {

    private BasicPersistenceService<Person> persistenceService = new BasicPersistenceService<>(Person.class);

    @Before
    public void setUp() throws IOException {
        persistenceService.deleteAll();
    }

    @Test
    public void testSaveRead() throws IOException, ClassNotFoundException {
        Person p = new Person("bla");

        persistenceService.save(p);

        Set<Person> all = persistenceService.getAll();
        assertThat(all, hasItems(p));
    }

    @Test
    public void testSaveReadMulti() throws IOException, ClassNotFoundException {
        Person p0 = new Person("bla0");
        Person p1 = new Person("bla1");
        Person p2 = new Person("bla2");

        persistenceService.save(p0);
        persistenceService.save(p1);
        persistenceService.save(p2);

        Set<Person> all = persistenceService.getAll();
        assertThat(all, hasItems(p0, p1, p2));
    }

}

