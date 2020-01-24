/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.test.items;

import lombok.EqualsAndHashCode;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Date;

/**
 * Javabean style class with bound properties
 *
 * @author oge
 * @since 06/03/2017
 */
@EqualsAndHashCode(callSuper = true)
public class ItemBean extends AbstractItem {

    private String name;
    private Date date;
    private long longInteger;

    private final PropertyChangeSupport propertyChangeSupport;
    {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public ItemBean() {
        init();
    }

    @Override
    public String getName() {
        return name ;
    }
    @Override
    public void setName(String name) {
        String oldName = this.name ;
        this.name = name ;
        propertyChangeSupport.firePropertyChange("name", oldName, name);
    }

    @Override
    public Date getDate() {
        return date;
    }
    @Override
    public void setDate(Date date) {
        Date oldDate = this.date;
        this.date = date;
        propertyChangeSupport.firePropertyChange("date", oldDate, date);
    }

    @Override
    public long getLongInteger() {
        return longInteger;
    }
    @Override
    public void setLongInteger(long longInteger) {
        long oldValue = this.longInteger;
        this.longInteger = longInteger;
        propertyChangeSupport.firePropertyChange("longInteger", oldValue, longInteger);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
}
