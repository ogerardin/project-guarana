/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.demo.javafx.hr.adapters;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.Configuration;
import com.ogerardin.business.sample.hr.model.Employee;
import com.ogerardin.business.sample.hr.model.Event;
import com.ogerardin.business.sample.hr.model.Leave;
import com.ogerardin.business.sample.hr.service.DomainManager;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Olivier
 * @since 26/05/15
 */
public class DomainManagerDb4OImpl implements DomainManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainManagerDb4OImpl.class);

    private final ObjectContainer objectContainer;

    private ObservableList<Employee> allEmployees;
    private ObservableList<Event> allEvents;
    private ObservableList<Leave> allLeaves;


    public DomainManagerDb4OImpl() {
        Configuration configuration = Db4o.newConfiguration();
        objectContainer = Db4o.openFile(configuration, "data.db4o");
    }

    @Override
    public void clearAll() {
        objectContainer.query(Employee.class).forEach(objectContainer::delete);
        objectContainer.query(Event.class).forEach(objectContainer::delete);
        objectContainer.commit();
    }

    public Collection<Employee> getAllEmployees() {
        if (allEmployees == null) {
            final ObjectSet<Employee> employees = objectContainer.query(Employee.class);
            allEmployees = getReplicatingObservableList(employees, objectContainer);
        }
        return allEmployees;
    }

    @Override
    public Collection<Event> getAllEvents() {
        if (allEvents == null) {
            final ObjectSet<Event> events = objectContainer.query(Event.class);
            allEvents = getReplicatingObservableList(events, objectContainer);
        }
        return allEvents;
    }

    public Collection<Leave> getAllLeaves() {
        if (allLeaves == null) {
            final ObjectSet<Leave> leaves = objectContainer.query(Leave.class);
            allLeaves = getReplicatingObservableList(leaves, objectContainer);
        }
        return allLeaves;
    }

    //TODO move somewhere else
    private static <T> ObservableList<T> getReplicatingObservableList(List<T> list, ObjectContainer objectContainer) {
        ObservableListWrapper<T> observableList = new ObservableListWrapper<>(list);
        observableList.addListener((ListChangeListener<T>) c -> {
            LOGGER.debug("change: " + c);
            while (c.next()) {
                if (c.wasAdded() || c.wasUpdated()) {
                    c.getAddedSubList().forEach(objectContainer::store);
                } else if (c.wasRemoved()) {
                    c.getRemoved().forEach(objectContainer::delete);
                } else {
                    LOGGER.warn("change not supported: " + c);
                }
            }
        });
        return observableList;
    }

    public Employee save(Employee person) {
        objectContainer.store(person);
        return person;
    }

    @Override
    public Event save(Event event) {
        objectContainer.store(event);
        return event;
    }

    @Override
    public void resetDemo() {
        clearAll();

        final Employee employee0 = new Employee("GERARDIN", "Olivier");
        save(employee0);
        final Employee employee1 = new Employee("MARCEAU", "Marcel");
        save(employee1);
        final Employee employee2 = new Employee("OBAMA", "Barack");
        save(employee2);

        save(new Event(employee0, new Date()));

        getAllEmployees().forEach(System.out::println);
        getAllEvents().forEach(System.out::println);
        getAllLeaves().forEach(System.out::println);
    }

    @Override
    public List<Leave> getLeavesByPerson(Employee employee) {
        return getAllLeaves().stream()
                .filter(l -> l.getEmployee() == employee)
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        new DomainManagerDb4OImpl().resetDemo();
    }

}
