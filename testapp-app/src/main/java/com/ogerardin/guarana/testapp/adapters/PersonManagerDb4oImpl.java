package com.ogerardin.guarana.testapp.adapters;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.config.Configuration;
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
        objectContainer = Db4o.openFile(configuration,"data.db4o");
    }

    public Collection<Person> getAllpersons() {
        return objectContainer.query(Person.class);
    }

    public Person save(Person person) {
        objectContainer.store(person);
        return person;
    }
}
