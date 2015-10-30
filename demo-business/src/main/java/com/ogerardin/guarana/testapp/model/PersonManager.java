/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.testapp.model;

import java.util.Collection;

/**
 * @author Olivier
 * @since 26/05/15
 */
public interface PersonManager {

    Collection<Person> getAllPersons();

    Collection<Event> getAllEvents();

    Person save(Person person);

    Event save(Event event);
}
