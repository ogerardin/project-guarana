/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui.impl;

import com.ogerardin.guarana.core.config.ClassConfiguration;
import com.ogerardin.guarana.core.config.ToString;
import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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

        final TargetStringConverter<T> converter = new TargetStringConverter<>(jfxUiManager, clazz);
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


    private static class TargetStringConverter<X> extends StringConverter<X> {
        private final JfxUiManager jfxUiManager;
        private final Class<X> clazz;

        public TargetStringConverter(JfxUiManager jfxUiManager, Class<X> clazz) {
            this.jfxUiManager = jfxUiManager;
            this.clazz = clazz;
        }

        @Override
        public String toString(X object) {
            return jfxUiManager.getConfiguration().forClass(clazz).toString(object);
        }

        @Override
        public X fromString(String string) {
            try {
                final Constructor<X> constructor = clazz.getConstructor(String.class);
                final X object = constructor.newInstance(string);
                return object;
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Can't convert from String to " + clazz + ": no contructor "
                        + clazz.getName() + "(String) found");
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Contructor invocation " + clazz.getName() + "(\"" + string + "\") failed", e);
            }
        }
    }
}
