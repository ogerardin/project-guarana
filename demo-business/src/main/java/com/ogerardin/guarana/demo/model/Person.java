/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.demo.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Olivier
 * @since 26/05/15
 */
@Getter
@Setter
public class Person {

    private String lastName;

    private String firstName;

    public Person(String lastName, String firstName) {
        this.lastName = lastName;
        this.firstName = firstName;
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
