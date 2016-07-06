/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.demo.model;

/**
 * @author Olivier
 * @since 26/05/15
 */
public class Person {
    private String lastName;
    private String firstName;

    public Person(String lastName, String firstName) {
        this.lastName = lastName;
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setFirstName(String firstName) {
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
        return firstName + " " + lastName;
    }

    public String getFullNameLastFirst() {
        return getLastName() + ", " + getFirstName();
    }

    public String getFullNameFirstLast() {
        return getFirstName() + " " + getLastName();
    }
}
