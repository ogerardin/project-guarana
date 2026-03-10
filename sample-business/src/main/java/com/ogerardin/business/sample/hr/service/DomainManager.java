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
 * Service interface for managing HR domain objects (Employee, Event, Leave).
 * Provides CRUD operations and demo data reset functionality.
 *
 * @author Olivier Gérardin
 * @since 1.0
 */
public interface DomainManager {

    /**
     * Clears all data from the persistence store.
     */
    void clearAll();

    /**
     * Returns all employees in the system.
     */
    Collection<Employee> getAllEmployees();

    /**
     * Returns all events in the system.
     */
    Collection<Event> getAllEvents();

    /**
     * Saves an employee to the persistence store.
     */
    Employee save(Employee employee);

    /**
     * Saves an event to the persistence store.
     */
    Event save(Event event);

    /**
     * Returns all leaves associated with the specified employee.
     */
    List<Leave> getLeavesByPerson(Employee employee);

    /**
     * Resets the system with sample demo data.
     */
    void resetDemo();
}
