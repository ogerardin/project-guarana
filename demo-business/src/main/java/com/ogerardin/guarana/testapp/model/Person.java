/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.testapp.model;

/**
 * @author Olivier
 * @since 26/05/15
 */
public class Person {
    private String lastName;
    private String firstName;

    public Person(String firstName, String lastName) {
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
        return "Person{" +
                "lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                '}';
    }
}
