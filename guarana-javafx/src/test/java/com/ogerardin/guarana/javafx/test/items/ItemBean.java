/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.test.items;

import lombok.EqualsAndHashCode;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Date;

/**
 * Javabean style class with PropertyChangeSupport
 *
 * @author oge
 * @since 06/03/2017
 */
@EqualsAndHashCode
public class ItemBean {

    private String name = "";
    private Date date = new Date();

    private final PropertyChangeSupport propertySupport ;
    {
        this.propertySupport = new PropertyChangeSupport(this);
    }

    public ItemBean() {
        setName("default");
        setDate(new Date());
    }

    public String getName() {
        return name ;
    }
    public void setName(String name) {
        String oldName = this.name ;
        this.name = name ;
        propertySupport.firePropertyChange("name", oldName, name);
    }

    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        Date oldDate = this.date;
        this.date = date;
        propertySupport.firePropertyChange("date", oldDate, date);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }
}
