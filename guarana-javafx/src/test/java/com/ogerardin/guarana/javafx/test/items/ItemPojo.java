/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.test.items;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.nio.file.Path;
import java.util.Date;

/**
 * POJO style class (without PropertyChangeSupport)
 *
 * @author oge
 * @since 06/03/2017
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ItemPojo extends AbstractItem {

    public String name;
    public Date date;
    private long longInteger;
    public Path path;

    public ItemPojo() {
        init();
    }
}
