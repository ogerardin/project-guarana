/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.testapp.model;

import java.util.Date;

/**
 * @author oge
 * @since 06/10/2015
 */
public class Event {

    private Person person;
    private Date date;
    private String memo;

    public Event() {
    }

    public Event(Date date) {
        this.date = date;
    }

    public Event(Date date, Person person) {
        this.date = date;
        this.person = person;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    @Override
    public String toString() {
        return "Event{" +
                "person=" + person +
                ", date=" + date +
                ", memo='" + memo + '\'' +
                '}';
    }
}
