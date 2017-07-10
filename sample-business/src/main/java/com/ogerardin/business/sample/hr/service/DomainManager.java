/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.business.sample.hr.service;

import com.ogerardin.business.sample.hr.model.Employee;
import com.ogerardin.business.sample.hr.model.Event;
import com.ogerardin.business.sample.hr.model.Leave;

import java.util.Collection;
import java.util.List;

/**
 * @author Olivier
 * @since 26/05/15
 */
public interface DomainManager {

    void clearAll();

    Collection<Employee> getAllEmployees();

    Collection<Event> getAllEvents();

    Employee save(Employee employee);

    Event save(Event event);

    List<Leave> getLeavesByPerson(Employee employee);

    void resetDemo();
}
