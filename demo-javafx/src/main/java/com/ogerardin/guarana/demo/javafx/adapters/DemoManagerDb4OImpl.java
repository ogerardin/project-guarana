/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.demo.javafx.adapters;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.config.Configuration;
import com.ogerardin.guarana.demo.model.DemoManager;
import com.ogerardin.guarana.demo.model.Event;
import com.ogerardin.guarana.demo.model.Person;

import java.util.Collection;

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

    public void clearAll() {
        objectContainer.query(Person.class).forEach(objectContainer::delete);
        objectContainer.query(Event.class).forEach(objectContainer::delete);
        objectContainer.commit();
    }

    public Collection<Person> getAllPersons() {
        return objectContainer.query(Person.class);
    }

    @Override
    public Collection<Event> getAllEvents() {
        return objectContainer.query(Event.class);
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
}
