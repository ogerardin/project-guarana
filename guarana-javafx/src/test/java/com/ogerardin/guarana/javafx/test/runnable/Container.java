/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.test.runnable;

import lombok.Data;

import java.util.Date;

/**
 * @author oge
 * @since 06/03/2017
 */
@Data
public class Container {
    public String name;

    public Date date = new Date();

    public void dump() {
        System.out.println(this.toString());
    }

}
