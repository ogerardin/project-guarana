/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.demo.javafx.hr.adapters;

import com.ogerardin.business.sample.hr.model.Employee;
import com.ogerardin.business.sample.hr.model.Event;
import com.ogerardin.business.sample.hr.model.Leave;
import com.ogerardin.business.sample.hr.service.DomainManager;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.NavigableSet;
import java.util.stream.Collectors;

/**
 * MapDB-based implementation of {@link DomainManager} for persisting
 * HR domain objects (Employee, Event, Leave) to disk.
 *
 * @author Olivier Gérardin
 * @since 1.0
 */
public class DomainManagerMapDBImpl implements DomainManager {

    private final DB db;

    private ObservableList<Employee> allEmployees;
    private ObservableList<Event> allEvents;
    private ObservableList<Leave> allLeaves;


    /**
     * Creates a new DomainManagerMapDBImpl with a file-based MapDB database.
     */
    public DomainManagerMapDBImpl() {
        db = DBMaker.fileDB("demo.db")
                .fileMmapEnableIfSupported()
                .transactionEnable()      // Enable WAL for crash protection
                .closeOnJvmShutdown()     // Automatic cleanup on JVM exit
                .fileLockDisable()        // Disable file locking to avoid conflicts
                .make();
        
        // Create collections if they don't exist
        db.treeSet("employees", Serializer.JAVA).createOrOpen();
        db.treeSet("events", Serializer.JAVA).createOrOpen();
        db.treeSet("leaves", Serializer.JAVA).createOrOpen();
    }

    @Override
    /**
     * Clears all data from the database.
     */
    public void clearAll() {
        @SuppressWarnings("unchecked")
        NavigableSet<Employee> employees = (NavigableSet<Employee>) db.treeSet("employees", Serializer.JAVA).createOrOpen();
        @SuppressWarnings("unchecked")
        NavigableSet<Event> events = (NavigableSet<Event>) db.treeSet("events", Serializer.JAVA).createOrOpen();
        employees.clear();
        events.clear();
        db.commit();
    }

    /**
     * Returns all employees in the database as an observable list.
     */
    public Collection<Employee> getAllEmployees() {
        if (allEmployees == null) {
            @SuppressWarnings("unchecked")
            NavigableSet<Employee> employees = (NavigableSet<Employee>) db.treeSet("employees", Serializer.JAVA).createOrOpen();
            allEmployees = getReplicatingObservableList(employees, "employees");
        }
        return allEmployees;
    }

    @Override
    /**
     * Returns all events in the database as an observable list.
     */
    public Collection<Event> getAllEvents() {
        if (allEvents == null) {
            @SuppressWarnings("unchecked")
            NavigableSet<Event> events = (NavigableSet<Event>) db.treeSet("events", Serializer.JAVA).createOrOpen();
            allEvents = getReplicatingObservableList(events, "events");
        }
        return allEvents;
    }

    /**
     * Returns all leaves in the database as an observable list.
     */
    public Collection<Leave> getAllLeaves() {
        if (allLeaves == null) {
            @SuppressWarnings("unchecked")
            NavigableSet<Leave> leaves = (NavigableSet<Leave>) db.treeSet("leaves", Serializer.JAVA).createOrOpen();
            allLeaves = getReplicatingObservableList(leaves, "leaves");
        }
        return allLeaves;
    }

    //TODO move somewhere else
    private <T> ObservableList<T> getReplicatingObservableList(NavigableSet<T> set, String setName) {
        ObservableList<T> observableList = FXCollections.observableArrayList(set.stream().collect(Collectors.toList()));
        observableList.addListener((ListChangeListener<T>) c -> {
            System.out.println("change: " + c);
            while (c.next()) {
                if (c.wasAdded()) {
                    @SuppressWarnings("unchecked")
                    NavigableSet<T> dbSet = (NavigableSet<T>) db.treeSet(setName, Serializer.JAVA).createOrOpen();
                    c.getAddedSubList().forEach(dbSet::add);
                    db.commit();
                } else if (c.wasRemoved()) {
                    @SuppressWarnings("unchecked")
                    NavigableSet<T> dbSet = (NavigableSet<T>) db.treeSet(setName, Serializer.JAVA).createOrOpen();
                    c.getRemoved().forEach(dbSet::remove);
                    db.commit();
                } else {
                    System.err.println("change not supported: " + c);
                }
            }
        });
        return observableList;
    }

    /**
     * Saves an employee to the database.
     */
    public Employee save(Employee employee) {
        @SuppressWarnings("unchecked")
        NavigableSet<Employee> employees = (NavigableSet<Employee>) db.treeSet("employees", Serializer.JAVA).createOrOpen();
        employees.add(employee);
        db.commit();
        return employee;
    }

    @Override
    /**
     * Saves an event to the database.
     */
    public Event save(Event event) {
        @SuppressWarnings("unchecked")
        NavigableSet<Event> events = (NavigableSet<Event>) db.treeSet("events", Serializer.JAVA).createOrOpen();
        events.add(event);
        db.commit();
        return event;
    }

    @Override
    /**
     * Resets the database with sample demo data.
     */
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
    /**
     * Returns all leaves associated with the specified employee.
     */
    public List<Leave> getLeavesByPerson(Employee employee) {
        return getAllLeaves().stream()
                .filter(l -> l.getEmployee() == employee)
                .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        new DomainManagerMapDBImpl().resetDemo();
    }

}
