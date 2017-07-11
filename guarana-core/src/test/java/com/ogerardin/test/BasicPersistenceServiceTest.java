/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.test;

import com.ogerardin.guarana.core.persistence.basic.BasicPersistenceService;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by olivier on 18/05/2017.
 */
public class BasicPersistenceServiceTest {

    private static final Item P_0 = new Item("bla0");
    private static final Item P_1 = new Item("bla1");
    private static final Item P_2 = new Item("bla2");

    private BasicPersistenceService<Item> persistenceService = new BasicPersistenceService<>(Item.class);

    @Before
    public void setUp() throws IOException {
        persistenceService.deleteAll();
    }

    @Test
    public void testSaveRead() throws IOException, ClassNotFoundException {
        persistenceService.save(P_0);

        Set<Item> all = persistenceService.getAll();
        assertThat(all, hasItems(P_0));
    }

    @Test
    public void testSaveReadMulti() throws IOException, ClassNotFoundException {
        persistenceService.save(P_0, P_1, P_2);

        Set<Item> all = persistenceService.getAll();
        assertThat(all, hasItems(P_0, P_1, P_2));
    }

    @Test
    public void testDelete() throws IOException, ClassNotFoundException {
        persistenceService.deleteAll();
        persistenceService.save(P_0, P_1, P_2);

        persistenceService.delete(P_1);

        Set<Item> all = persistenceService.getAll();
        assertThat(all, not(hasItems(P_1)));
    }

    @Test
    public void testDeleteAll() throws IOException, ClassNotFoundException {
        persistenceService.deleteAll();
        Set<Item> all = persistenceService.getAll();
        assertTrue(all.isEmpty());
    }


}

