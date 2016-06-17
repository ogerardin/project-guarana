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

    private ObjectProperty<T> targetProperty = new SimpleObjectProperty<>();


    public DefaultJfxEmbeddedInstanceUI(JfxUiManager jfxUiManager, Class<T> clazz) {
        this.jfxUiManager = jfxUiManager;
        this.clazz = clazz;

        //final TargetStringConverter<T> converter = new TargetStringConverter<>(jfxUiManager, clazz);
        final StringConverter<T> converter = Bindings.getStringConverter(clazz, jfxUiManager.getConfiguration());
        textProperty().bindBidirectional(targetProperty, converter);

        addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() == KeyCode.F5) {
                final T value = targetProperty.getValue();
                final String s = converter.toString(value);
                textProperty().setValue(s);
            }
        });
    }

    @Override
    public ObjectProperty<T> targetProperty() {
        return targetProperty;
    }

    @Override
    public void setTarget(T target) {
        targetProperty.setValue(target);
    }

    @Override
    public TextField render() {
        return this;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        setEditable(!readOnly);
    }


}
