/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.binding;

import com.google.common.base.Converter;
import com.ogerardin.guarana.core.config.ClassConfiguration;
import com.ogerardin.guarana.core.config.Configuration;
import com.ogerardin.guarana.core.metadata.PropertyInformation;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import jfxtras.labs.scene.control.BeanPathAdapter;

/**
 * @author oge
 * @since 14/01/2016
 */
public enum Bindings {

    ;

    public static <T> void bindTextField(Configuration configuration, TextField textField, PropertyInformation propertyInformation, T target) {
        if (textField.isEditable()) {
            BeanPathAdapter<T> beanPathAdapter = new BeanPathAdapter<>(target);
            String propertyName = propertyInformation.getName();
            beanPathAdapter.bindBidirectional(propertyName, textField.textProperty());
        } else {
            //FIXME we should bind (unidirectionally) and not just set property value
            try {
                final Object value = propertyInformation.getReadMethod().invoke(target);
                fieldSetValue(configuration, textField, propertyInformation, value);
            } catch (Exception ignored) {
                ignored.printStackTrace(System.err);
            }
        }
    }

    public static void fieldSetValue(Configuration configuration, TextField textField, PropertyInformation propertyInformation, Object value) {
        ClassConfiguration classConfig = configuration.forClass(propertyInformation.getPropertyType());
        textField.setText(classConfig.toString(value));
    }

    public static <A, B> void bindBidirectional(ObjectProperty<A> aProperty, ObjectProperty<B> bProperty,
                                                Converter<A, B> converter) {
        aProperty.addListener((observable, oldValue, newValue) -> {
            B bValue = converter.convert(newValue);
            bProperty.setValue(bValue);
        });
        bProperty.addListener((observable, oldValue, newValue) -> {
            A aValue = converter.reverse().convert(newValue);
            aProperty.setValue(aValue);
        });
    }

    public static <T> StringConverter<T> getStringConverter(Class<T> paramType, Configuration configuration) {
        return new StringConverter<T>() {
            @Override
            public String toString(T object) {
                ClassConfiguration<T> classConfig = configuration.forClass(paramType);
                return classConfig.toString(object);
            }

            @Override
            public T fromString(String string) {
                return null;
            }
        };
    }
}
