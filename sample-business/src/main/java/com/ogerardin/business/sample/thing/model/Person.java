/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.business.sample.thing.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author oge
 * @since 12/01/2017
 */
@EqualsAndHashCode
@ToString
public class Person implements Serializable{

    private final String name;

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
