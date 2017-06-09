/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.domain0.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Olivier
 * @since 26/05/15
 */
@Getter
@Setter
public class Employee {

    private String lastName;

    private String firstName;

    public Employee(String lastName, String firstName) {
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
