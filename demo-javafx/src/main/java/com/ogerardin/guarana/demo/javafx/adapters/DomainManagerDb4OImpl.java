/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.demo.javafx.adapters;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.Configuration;
import com.ogerardin.guarana.demo.model.DomainManager;
import com.ogerardin.guarana.demo.model.Event;
import com.ogerardin.guarana.demo.model.Leave;
import com.ogerardin.guarana.demo.model.Person;
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

    private ObservableList<Person> allPersons;
    private ObservableList<Event> allEvents;
    private ObservableList<Leave> allLeaves;


    public DomainManagerDb4OImpl() {
        Configuration configuration = Db4o.newConfiguration();
        objectContainer = Db4o.openFile(configuration, "data.db4o");
    }

    @Override
    public void clearAll() {
        objectContainer.query(Person.class).forEach(objectContainer::delete);
        objectContainer.query(Event.class).forEach(objectContainer::delete);
        objectContainer.commit();
    }

    public Collection<Person> getAllPersons() {
        if (allPersons == null) {
            final ObjectSet<Person> persons = objectContainer.query(Person.class);
            allPersons = getReplicatingObservableList(persons, objectContainer);
        }
        return allPersons;
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
                    c.getAddedSubList().stream().forEach(objectContainer::store);
                } else if (c.wasRemoved()) {
                    c.getRemoved().stream().forEach(objectContainer::delete);
                } else {
                    LOGGER.warn("change not supported: " + c);
                }
            }
        });
        return observableList;
    }

    public Person save(Person person) {
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

        final Person person0 = new Person("GERARDIN", "Olivier");
        save(person0);
        final Person person1 = new Person("MARCEAU", "Marcel");
        save(person1);
        final Person person2 = new Person("OBAMA", "Barack");
        save(person2);

        save(new Event(person0, new Date()));

        getAllPersons().forEach(System.out::println);
        getAllEvents().forEach(System.out::println);
        getAllLeaves().forEach(System.out::println);
    }

    @Override
    public List<Leave> getLeavesByPerson(Person person) {
        return getAllLeaves().stream()
                .filter(l -> l.getPerson() == person)
                .collect(Collectors.toList());
    }

}
