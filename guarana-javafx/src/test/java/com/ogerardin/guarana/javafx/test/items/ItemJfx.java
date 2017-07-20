/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.test.items;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Data;

import java.util.Date;

/**
 * JavaFX style class with JavaFX properties
 *
 * @author oge
 * @since 06/03/2017
 */
@Data
public class ItemJfx {

    private StringProperty nameProperty = new SimpleStringProperty();
    private ObjectProperty<Date> dateProperty = new SimpleObjectProperty<Date>();

    public String getName() {
        return nameProperty.get();
    }

    public StringProperty nameProperty() {
        return nameProperty;
    }

    public void setName(String name) {
        this.nameProperty.set(name);
    }

    public Date getDate() {
        return dateProperty.get();
    }

    public ObjectProperty<Date> dateProperty() {
        return dateProperty;
    }

    public void setDate(Date date) {
        this.dateProperty.set(date);
    }
}
