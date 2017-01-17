/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.test.model;

import java.util.List;

/**
 * @author oge
 * @since 12/01/2017
 */
public interface Manager {

    List<Person> getAllPersons();

    List<Thing> getThingsByPerson(Person person);

    Person savePerson(Person person);

    Thing saveThing(Thing thing);
}
