/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.test;

import com.ogerardin.guarana.core.persistence.basic.BasicPersistenceService;
import com.ogerardin.guarana.core.test.domain.Item;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by olivier on 18/05/2017.
 */
public class BasicPersistenceServiceTest {

    private static final Item P_0 = new Item("bla 0");
    private static final Item P_1 = new Item("bla 1");
    private static final Item P_2 = new Item("bla 2");

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
//        assertThat(all, hasItems(P_0, P_1, P_2));
        HashSet<Item> expected = new HashSet<>(Arrays.asList(P_0, P_1, P_2));
        assertThat(all, Matchers.equalTo(expected));
    }

    @Test
    public void testDelete() throws IOException, ClassNotFoundException {
        persistenceService.save(P_0, P_1, P_2);

        persistenceService.delete(P_1);

        Set<Item> all = persistenceService.getAll();
        assertThat(all, not(hasItems(P_1)));
    }

    @Test
    public void testDeleteAll() throws IOException, ClassNotFoundException {
        persistenceService.save(P_0, P_1, P_2);

        persistenceService.deleteAll();

        Set<Item> all = persistenceService.getAll();
        assertTrue(all.isEmpty());
    }


}

