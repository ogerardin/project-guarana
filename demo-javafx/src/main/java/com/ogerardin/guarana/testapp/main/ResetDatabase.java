package com.ogerardin.guarana.testapp.main;

import com.ogerardin.guarana.testapp.adapters.PersonManagerDb4oImpl;
import com.ogerardin.guarana.testapp.model.Person;
import com.ogerardin.guarana.testapp.model.PersonManager;

/**
 * Created by Olivier on 27/05/15.
 */
public class ResetDatabase {

    public static void main(String args[]) {
        PersonManager personManager = new PersonManagerDb4oImpl();

        ((PersonManagerDb4oImpl)personManager).clearAll();

        personManager.save(new Person("GERARDIN", "Olivier"));
        personManager.save(new Person("MARCEAU", "Marcel"));
        personManager.save(new Person("OBAMA", "Barack"));

        personManager.getAllPersons().forEach(System.out::println);

    }


}
