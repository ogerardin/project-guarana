/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.demo.javafx.adapters;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.Configuration;
import com.ogerardin.guarana.demo.model.DemoManager;
import com.ogerardin.guarana.demo.model.Event;
import com.ogerardin.guarana.demo.model.Person;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Olivier
 * @since 26/05/15
 */
public class DemoManagerDb4OImpl implements DemoManager {

    private final ObjectContainer objectContainer;

    private ObservableList<Person> allPersons;
    private ObservableList<Event> allEvents;


    public DemoManagerDb4OImpl() {
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

    //TODO move somewhere else
    private static <T> ObservableList<T> getReplicatingObservableList(List<T> list, ObjectContainer objectContainer) {
        ObservableListWrapper<T> observableList = new ObservableListWrapper<>(list);
        observableList.addListener((ListChangeListener<T>) c -> {
            System.out.println(c);
            while (c.next()) {
                if (c.wasAdded() || c.wasUpdated()) {
                    c.getAddedSubList().stream().forEach(objectContainer::store);
                } else if (c.wasRemoved()) {
                    c.getRemoved().stream().forEach(objectContainer::delete);
                } else {
                    System.err.println("change not supported: " + c);
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

        save(new Event(new Date(), person0));

        getAllPersons().forEach(System.out::println);
        getAllEvents().forEach(System.out::println);

    }

}
