/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.demo.model;

import lombok.Data;

import java.util.Date;

/**
 * @author oge
 * @since 06/10/2015
 */
@Data
public class Event {

    private final Person person;

    private Date date;

    private String memo;

    public Event(Person person, Date date) {
        this.person = person;
        this.date = date;
    }

}
