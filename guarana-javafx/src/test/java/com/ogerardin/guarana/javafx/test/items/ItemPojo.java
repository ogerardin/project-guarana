/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.test.items;

import lombok.Data;

import java.util.Date;

/**
 * POJO style class (without PropertyChangeSupport)
 *
 * @author oge
 * @since 06/03/2017
 */
@Data
public class ItemPojo {

    public String name;
    public Date date;

    public ItemPojo() {
        setName("default");
        setDate(new Date());
    }
}
