/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.testapp.main;

import com.ogerardin.guarana.testapp.adapters.PersonManagerDb4oImpl;
import com.ogerardin.guarana.testapp.model.Event;
import com.ogerardin.guarana.testapp.model.Person;
import com.ogerardin.guarana.testapp.model.PersonManager;

import java.util.Date;

/**
 * @author Olivier
 * @since 27/05/15
 */
public class ResetDatabase {

    public static void main(String args[]) {
        PersonManager personManager = new PersonManagerDb4oImpl();

        ((PersonManagerDb4oImpl)personManager).clearAll();

        final Person person0 = new Person("GERARDIN", "Olivier");
        personManager.save(person0);
        final Person person1 = new Person("MARCEAU", "Marcel");
        personManager.save(person1);
        final Person person2 = new Person("OBAMA", "Barack");
        personManager.save(person2);

        personManager.save(new Event(new Date(), person0));

        personManager.getAllPersons().forEach(System.out::println);
        personManager.getAllEvents().forEach(System.out::println);
    }


}
