/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.test.items;

import java.util.Date;

public abstract class AbstractItem {

    void init() {
        setName("default");
        setDate(new Date());
        setLongInteger(10);
    }

    public abstract String getName();

    public abstract void setName(String name);

    public abstract Date getDate();

    public abstract void setDate(Date date);

    public abstract long getLongInteger();

    public abstract void setLongInteger(long longInteger);
}
