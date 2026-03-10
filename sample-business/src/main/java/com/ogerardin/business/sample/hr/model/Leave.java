/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.business.sample.hr.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author oge
 * @since 09/06/2016
 */
@Data
public class Leave implements Serializable {

    private final Employee employee;

    private Date start;

    private Date end;

    private double duration;

    public Leave(Employee employee, Date start, Date end) {
        this.employee = employee;
        this.start = start;
        this.end = end;
        computeDuration();
    }

    private void computeDuration() {
        final long millis = end.getTime() - start.getTime();
        final long days = millis / (24 * 60 * 60 * 1000);
        duration = days + 1;
    }

}
