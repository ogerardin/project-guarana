package com.ogerardin.guarana.testapp.model;

import com.google.common.base.MoreObjects;

/**
 * Created by Olivier on 26/05/15.
 */
public class Person {
    private final String lastName;
    private final String firstName;

    public Person(String firstName, String lastName) {
        this.lastName = lastName;
        this.firstName = firstName;
    }


    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("firstName", firstName)
                .add("lastName", lastName)
                .toString();
    }
}
