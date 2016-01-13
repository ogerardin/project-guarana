/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.demo.javafx.main;

import com.ogerardin.guarana.demo.javafx.adapters.DemoManagerDb4OImpl;
import com.ogerardin.guarana.demo.model.DemoManager;
import com.ogerardin.guarana.demo.model.Event;
import com.ogerardin.guarana.demo.model.Person;

import java.util.Date;

/**
 * @author Olivier
 * @since 27/05/15
 */
public class ResetDatabase {

    public static void main(String args[]) {
        DemoManager demoManager = new DemoManagerDb4OImpl();

        ((DemoManagerDb4OImpl) demoManager).clearAll();

        final Person person0 = new Person("GERARDIN", "Olivier");
        demoManager.save(person0);
        final Person person1 = new Person("MARCEAU", "Marcel");
        demoManager.save(person1);
        final Person person2 = new Person("OBAMA", "Barack");
        demoManager.save(person2);

        demoManager.save(new Event(new Date(), person0));

        demoManager.getAllPersons().forEach(System.out::println);
        demoManager.getAllEvents().forEach(System.out::println);
    }


}
