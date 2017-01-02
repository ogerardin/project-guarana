/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.demo.model.manager;

import com.ogerardin.guarana.demo.model.core.Event;
import com.ogerardin.guarana.demo.model.core.Leave;
import com.ogerardin.guarana.demo.model.core.Person;

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
