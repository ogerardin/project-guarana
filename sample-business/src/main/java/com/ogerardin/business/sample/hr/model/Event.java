/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.business.sample.hr.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents an HR event associated with an employee.
 * Events can include performance reviews, meetings, or other notable occurrences.
 *
 * @author Olivier Gérardin
 * @since 1.0
 */
@Data
@NoArgsConstructor
public class Event implements Serializable {

    private Employee employee;

    private Date date;

    private String memo;

    /**
     * Creates a new event for the specified employee on the given date.
     */
    public Event(Employee employee, Date date) {
        this.employee = employee;
        this.date = date;
    }

}
