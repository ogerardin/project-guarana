/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.test.model;

import lombok.Data;

/**
 * @author oge
 * @since 12/01/2017
 */
public class Person {

    private final String name;

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
