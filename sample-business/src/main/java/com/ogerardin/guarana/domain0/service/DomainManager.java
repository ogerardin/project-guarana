/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.domain0.service;

import com.ogerardin.guarana.domain0.model.Employee;
import com.ogerardin.guarana.domain0.model.Event;
import com.ogerardin.guarana.domain0.model.Leave;

import java.util.Collection;
import java.util.List;

/**
 * @author Olivier
 * @since 26/05/15
 */
public interface DomainManager {

    void clearAll();

    Collection<Employee> getAllPersons();

    Collection<Event> getAllEvents();

    Employee save(Employee person);

    Event save(Event event);

    List<Leave> getLeavesByPerson(Employee person);

    void resetDemo();
}
