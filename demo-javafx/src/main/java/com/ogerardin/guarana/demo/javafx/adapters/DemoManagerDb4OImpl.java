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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * @author Olivier
 * @since 26/05/15
 */
public class DemoManagerDb4OImpl implements DemoManager {

    private final ObjectContainer objectContainer;

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
        final ObjectSet<Person> persons = objectContainer.query(Person.class);
        // return a modifiable copy
        return new ArrayList<>(persons);
    }

    @Override
    public Collection<Event> getAllEvents() {
        final ObjectSet<Event> events = objectContainer.query(Event.class);
        // return a modifiable copy
        return new ArrayList<>(events);
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
