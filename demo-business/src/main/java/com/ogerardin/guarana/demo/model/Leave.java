/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.demo.model;

import java.util.Date;

/**
 * @author oge
 * @since 09/06/2016
 */
public class Leave {

    final Person person;
    Date start;
    Date end;

    double duration;

    public Leave(Person person, Date start, Date end) {
        this.person = person;
        this.start = start;
        this.end = end;
        computeDuration();
    }

    private void computeDuration() {
        final long millis = end.getTime() - start.getTime();
        final long days = millis / (24 * 60 * 60 * 1000);
        duration = days + 1;
    }

    public Person getPerson() {
        return person;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }
}
