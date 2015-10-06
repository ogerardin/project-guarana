/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.testapp.adapters;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.Configuration;
import com.ogerardin.guarana.testapp.model.Event;
import com.ogerardin.guarana.testapp.model.Person;
import com.ogerardin.guarana.testapp.model.PersonManager;

import java.util.Collection;

/**
 * Created by Olivier on 26/05/15.
 */
public class PersonManagerDb4oImpl implements PersonManager {

    private final ObjectContainer objectContainer;

    public PersonManagerDb4oImpl() {
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
