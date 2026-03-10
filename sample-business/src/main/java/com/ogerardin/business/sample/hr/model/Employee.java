/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.business.sample.hr.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Represents an employee in the HR system.
 * Contains basic personal information and computed full name formats.
 *
 * @author Olivier Gérardin
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee implements Serializable {

    private String lastName;

    private String firstName;

    /**
     * Returns the employee's full name in "Last, First" format.
     */
    public String getFullNameLastFirst() {
        return getLastName() + ", " + getFirstName();
    }

    /**
     * Returns the employee's full name in "First Last" format.
     */
    public String getFullNameFirstLast() {
        return getFirstName() + " " + getLastName();
    }
}
