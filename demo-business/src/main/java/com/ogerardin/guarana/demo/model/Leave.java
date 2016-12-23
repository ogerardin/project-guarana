/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.demo.model;

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

    private final Person person;

    private Date start;

    private Date end;

    @Getter
    @Setter
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

}
