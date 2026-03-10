/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.business.sample.hr.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents an employee leave/absence period.
 * Automatically computes the duration in days based on start and end dates.
 *
 * @author Olivier Gérardin
 * @since 1.0
 */
@Data
public class Leave implements Serializable {

    private final Employee employee;

    private Date start;

    private Date end;

    private double duration;

    /**
     * Creates a new leave record for the specified employee and date range.
     * Duration is automatically computed.
     */
    public Leave(Employee employee, Date start, Date end) {
        this.employee = employee;
        this.start = start;
        this.end = end;
        computeDuration();
    }

    /**
     * Computes the duration in days based on start and end dates.
     */
    private void computeDuration() {
        final long millis = end.getTime() - start.getTime();
        final long days = millis / (24 * 60 * 60 * 1000);
        duration = days + 1;
    }

}
