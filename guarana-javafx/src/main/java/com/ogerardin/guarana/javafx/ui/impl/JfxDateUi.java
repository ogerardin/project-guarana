/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui.impl;

import com.google.common.base.Converter;
import com.ogerardin.guarana.javafx.binding.Bindings;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Parent;
import javafx.scene.control.DatePicker;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Simple implementation of JfxInstanceUI<Date> by inheriting directly from DatePicker.
 * Suitable for using as en embedded UI for Date properties as follows:
 * <pre>
 *             config.forClass(Date.class).setEmbeddedUiClass(JfxDateUi.class);
 * </pre>
 *
 * @author olivier
 * @since 12/01/2016.
 */
public class JfxDateUi extends DatePicker implements JfxInstanceUI<Date> {

    private ObjectProperty<Date> targetProperty = new SimpleObjectProperty<>();

    public JfxDateUi() {
        Bindings.bindBidirectional(valueProperty(), targetProperty(), new LocalDateDateConverter());
    }

    @Override
    public void setTarget(Date target) {
        targetProperty().setValue(target);
    }

    @Override
    public Parent render() {
        return this;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        setEditable(!readOnly);
    }

    @Override
    public ObjectProperty<Date> targetProperty() {
        return targetProperty;
    }


    /**
     * Simple converter to convert Date to and from LocalDate
     */
    private static class LocalDateDateConverter extends Converter<LocalDate, Date> {
        @Override
        protected Date doForward(LocalDate localDate) {
            return (localDate == null) ? null :
                    Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        @Override
        protected LocalDate doBackward(Date date) {
            return (date == null) ? null :
                    date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
    }
}
