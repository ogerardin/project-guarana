/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.binding;

import com.google.common.base.Converter;
import com.ogerardin.guarana.core.config.ClassConfiguration;
import com.ogerardin.guarana.core.config.Configuration;
import com.ogerardin.guarana.core.metamodel.PropertyInformation;
import com.ogerardin.guarana.core.util.DefaultStringConverter;
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


    @Deprecated
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

    @Deprecated
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

    public static <T> StringConverter<T> getStringConverter(Class<T> clazz, Configuration configuration) {
        ClassConfiguration<T> classConfig = configuration.forClass(clazz);
        StringConverter<T> stringConverter = classConfig.getStringConverter();
        if (stringConverter != null) {
            return stringConverter;
        }
        else {
            return getDefaultStringConverter(clazz);
        }
    }

    public static <T> StringConverter<T> getDefaultStringConverter(Class<T> clazz) {
        return new DefaultStringConverter<>(clazz);
    }

}
