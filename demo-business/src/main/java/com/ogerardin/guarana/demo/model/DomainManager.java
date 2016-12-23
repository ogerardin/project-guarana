/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.demo.model;

import java.util.Collection;
import java.util.List;

/**
 * @author Olivier
 * @since 26/05/15
 */
public interface DomainManager {

    void clearAll();

    Collection<Person> getAllPersons();

    Collection<Event> getAllEvents();

    Person save(Person person);

    Event save(Event event);

    List<Leave> getLeavesByPerson(Person person);

    void resetDemo();
}
