package com.ogerardin.guarana.testapp.main;

import com.ogerardin.guarana.testapp.adapters.PersonManagerDb4oImpl;
import com.ogerardin.guarana.testapp.model.Person;
import com.ogerardin.guarana.testapp.model.PersonManager;

/**
 * Created by Olivier on 27/05/15.
 */
public class MainConsole {

    public static void main(String args[]) {
        PersonManager personManager = new PersonManagerDb4oImpl();

        Person person = new Person("Gérardin", "Olivier");
        personManager.save(person);

        for (Person p : personManager.getAllpersons()) {
            System.out.println(p);
        }

    }


}
