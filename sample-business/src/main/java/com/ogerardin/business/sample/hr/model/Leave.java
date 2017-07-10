/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.business.sample.hr.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author oge
 * @since 09/06/2016
 */
@Data
public class Leave {

    private final Employee employee;

    private Date start;

    private Date end;

    @Getter
    @Setter
    double duration;

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
