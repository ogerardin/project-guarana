/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.domain1.model;

import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author oge
 * @since 12/01/2017
 */
@EqualsAndHashCode
public class Person implements Serializable{

    private final String name;

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
