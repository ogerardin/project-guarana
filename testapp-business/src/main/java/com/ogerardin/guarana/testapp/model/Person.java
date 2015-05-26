package com.ogerardin.guarana.testapp.model;

/**
 * Created by Olivier on 26/05/15.
 */
public class Person {
    private final String lastName;
    private final String firstName;

    public Person(String lastName, String firstName) {
        this.lastName = lastName;
        this.firstName = firstName;
    }


    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }
}
