/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.business.sample.hr.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Olivier
 * @since 26/05/15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee implements Serializable {

    private String lastName;

    private String firstName;

    public String getFullNameLastFirst() {
        return getLastName() + ", " + getFirstName();
    }

    public String getFullNameFirstLast() {
        return getFirstName() + " " + getLastName();
    }
}
