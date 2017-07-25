/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.test.items;

import javafx.beans.property.*;
import lombok.Data;

import java.util.Date;

/**
 * JavaFX style class with {@link javafx.beans.property.Property} properties
 *
 * @author oge
 * @since 06/03/2017
 */
@Data
public class ItemJfx extends AbstractItem {

    // JavaFX property wrappers
    private StringProperty name = new SimpleStringProperty();
    private ObjectProperty<Date> date = new SimpleObjectProperty<Date>();
    private LongProperty longInteger = new SimpleLongProperty();

    public ItemJfx() {
        init();
    }

    public StringProperty nameProperty() {
        return name;
    }
    public ObjectProperty<Date> dateProperty() {
        return date;
    }
    public LongProperty longIntegerProperty() {
        return longInteger;
    }

    public long getLongInteger() {
        return longInteger.get();
    }
    public void setLongInteger(long longInteger) {
        this.longInteger.set(longInteger);
    }

    public String getName() {
        return name.get();
    }
    public void setName(String name) {
        this.name.set(name);
    }

    public Date getDate() {
        return date.get();
    }
    public void setDate(Date date) {
        this.date.set(date);
    }


}
