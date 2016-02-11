/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.demo.javafx.ui;

import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Parent;
import javafx.scene.control.DatePicker;

import java.time.LocalDate;

/**
 * @author olivier
 * @since 12/01/2016.
 */
public class JfxDateUi extends DatePicker implements JfxInstanceUI<LocalDate> {

    @Override
    public void setTarget(LocalDate target) {
//        LocalDate value = (target == null) ? null :
//                target.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        setValue(target);
    }

    @Override
    public Parent render() {
        return this;
    }


    @Override
    public ObjectProperty<LocalDate> targetProperty() {
        return valueProperty();
    }
}
