/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui.impl;

import com.ogerardin.guarana.core.config.ClassConfiguration;
import com.ogerardin.guarana.core.config.ToString;
import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.binding.Bindings;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

/**
 * An implementation of JfxInstanceUI using a TextField, intended for use as an embedded field.
 * Conversion from type {@link T} to String for display is handled by {@link ClassConfiguration#toString(Object)},
 * which uses the {@link Object#toString()} by default, but may be overridden by calling {@link ClassConfiguration#setToString(ToString)}
 * as in the following example:
 * <pre>
 * configuration.forClass(Person.class).setToString(Person::getFullNameLastFirst);
 * </pre>
 * Conversion from String to type {@link T} assumes T has a public constructor that takes a String as only argument.
 *
 * @author olivier
 * @since 11/02/2016.
 */
public class DefaultJfxEmbeddedInstanceUI<T> extends TextField implements JfxInstanceUI<T> {

    private final JfxUiManager jfxUiManager;
    private final Class<T> clazz;

    private ObjectProperty<T> boundObjectProperty = new SimpleObjectProperty<>(this, "boundObject");


    public DefaultJfxEmbeddedInstanceUI(JfxUiManager jfxUiManager, Class<T> clazz) {
        this.jfxUiManager = jfxUiManager;
        this.clazz = clazz;

        if (clazz == String.class) {
            //binding with a String property: no converter required
            textProperty().bindBidirectional((Property<String>) boundObjectProperty());
        } else {
            final StringConverter<T> converter = Bindings.getStringConverter(clazz, jfxUiManager.getConfiguration());
            textProperty().bindBidirectional(boundObjectProperty, converter);
        }

//        textProperty().addListener((observable, oldValue, newValue) -> {
//            System.out.println("text changed: " + oldValue + " --> " + newValue);
//        });
//
//        boundObjectProperty().addListener((observable, oldValue, newValue) -> {
//            System.out.println("bound object changed: " + oldValue + " --> " + newValue);
//        });

    }

    @Override
    public ObjectProperty<T> boundObjectProperty() {
        return boundObjectProperty;
    }

    @Override
    public void bind(T object) {
        boundObjectProperty.setValue(object);
        //textProperty().setValue(object.toString());
    }

    @Override
    public TextField getRendering() {
        return this;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        setEditable(!readOnly);
    }


}
