/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.binding;

import com.google.common.base.Converter;
import com.ogerardin.guarana.core.config.ClassConfiguration;
import com.ogerardin.guarana.core.config.Configuration;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.TextField;
import jfxtras.labs.scene.control.BeanPathAdapter;

import java.beans.PropertyDescriptor;

/**
 * @author oge
 * @since 14/01/2016
 */
public class Bindings {

    public static <T> void bindTextField(Configuration configuration, TextField textField, PropertyDescriptor propertyDescriptor, T target) {
        if (textField.isEditable()) {
            BeanPathAdapter<T> beanPathAdapter = new BeanPathAdapter<>(target);
            String propertyName = propertyDescriptor.getName();
            beanPathAdapter.bindBidirectional(propertyName, textField.textProperty());
        } else {
            //FIXME we should bind (unidirectionally) and not just set property value
            try {
                final Object value = propertyDescriptor.getReadMethod().invoke(target);
                fieldSetValue(configuration, textField, propertyDescriptor, value);
            } catch (Exception ignored) {
                ignored.printStackTrace(System.err);
            }
        }
    }

    public static void fieldSetValue(Configuration configuration, TextField textField, PropertyDescriptor propertyDescriptor, Object value) {
        ClassConfiguration classConfig = configuration.forClass(propertyDescriptor.getPropertyType());
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
}