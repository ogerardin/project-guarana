/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.domain1.model;

/**
 * @author oge
 * @since 12/01/2017
 */
public class Thing {

    private final String name;

    private final Person owner;

    public Thing(String name, Person owner) {
        this.name = name;
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public Person getOwner() {
        return owner;
    }

}
