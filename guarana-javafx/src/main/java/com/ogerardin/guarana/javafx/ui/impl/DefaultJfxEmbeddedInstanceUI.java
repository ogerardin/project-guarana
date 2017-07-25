/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui.impl;

import com.ogerardin.guarana.core.config.ClassConfiguration;
import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.binding.Bindings;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

/**
 * An implementation of JfxInstanceUI by extending TextField, intended for use as an embedded field.
 *
 * Conversion from type {@link P} to String for display is handled by {@link ClassConfiguration#toString(Object)},
 * which uses the {@link Object#toString()} by default, but may be overridden by calling {@link ClassConfiguration#setToString(ToString)}
 * as in the following example:
 * <pre>
 * configuration.getClassInformation(Person.class).setToString(Person::getFullNameLastFirst);
 * </pre>
 *
 * @param <P> the type of object that will be bound to this UI
 * @author olivier
 * @since 11/02/2016.
 */
public class DefaultJfxEmbeddedInstanceUI<P> extends TextField implements JfxInstanceUI<P> {

    private final JfxUiManager jfxUiManager;
    private final Class<P> clazz;

    private ObjectProperty<P> boundObjectProperty = new SimpleObjectProperty<>(this, "boundObject");

    public DefaultJfxEmbeddedInstanceUI(JfxUiManager jfxUiManager, Class<P> clazz) {
        this.jfxUiManager = jfxUiManager;
        this.clazz = clazz;

        if (clazz == String.class) {
            // this UI is for a String property: no converter required
            //noinspection unchecked
            textProperty().bindBidirectional((Property<String>) boundObjectProperty());
        } else {
            final StringConverter<P> converter = Bindings.getStringConverter(clazz, jfxUiManager.getConfiguration());
            textProperty().bindBidirectional(boundObjectProperty, converter);
        }

        textProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("DefaultJfxEmbeddedInstanceUI: text changed: " + oldValue + " --> " + newValue);
        });

        boundObjectProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("DefaultJfxEmbeddedInstanceUI: bound object changed: " + oldValue + " --> " + newValue);
        });

    }

    @Override
    public ObjectProperty<P> boundObjectProperty() {
        return boundObjectProperty;
    }

    @Override
    public void bind(P object) {
        boundObjectProperty.setValue(object);
        //textProperty().setValue(object.toString());
    }

    @Override
    public TextField getRendered() {
        return this;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        setEditable(!readOnly);
    }


}
