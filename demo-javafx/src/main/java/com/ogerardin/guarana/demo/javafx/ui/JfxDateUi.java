/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.demo.javafx.ui;

import com.ogerardin.guarana.core.ui.InstanceUI;
import javafx.scene.Parent;
import javafx.scene.control.DatePicker;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author olivier
 * @since 12/01/2016.
 */
public class JfxDateUi extends DatePicker implements InstanceUI<Parent, Date> {

    @Override
    public void setTarget(Date target) {
        LocalDate value = target.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        setValue(value);
    }

    @Override
    public Parent render() {
        return this;
    }
}
