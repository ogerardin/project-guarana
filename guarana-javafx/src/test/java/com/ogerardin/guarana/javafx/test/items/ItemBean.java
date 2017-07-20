/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.test.items;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Date;

/**
 * Javabean style class with PropertyChangeSupport
 *
 * @author oge
 * @since 06/03/2017
 */
public class ItemBean {

    private String name;
    private Date date;

    private final PropertyChangeSupport propertySupport ;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        Date oldDate = this.date;
        this.date = date;
        propertySupport.firePropertyChange("date", oldDate, date);
    }

    public ItemBean() {
        this.propertySupport = new PropertyChangeSupport(this);
    }

    public String getName() {
        return name ;
    }

    public void setName(String name) {
        String oldName = this.name ;
        this.name = name ;
        propertySupport.firePropertyChange("name", oldName, name);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }
}
