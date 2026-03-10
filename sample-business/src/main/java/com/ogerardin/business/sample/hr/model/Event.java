/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.business.sample.hr.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author oge
 * @since 06/10/2015
 */
@Data
@NoArgsConstructor
public class Event implements Serializable {

    private Employee employee;

    private Date date;

    private String memo;

    public Event(Employee employee, Date date) {
        this.employee = employee;
        this.date = date;
    }

}
