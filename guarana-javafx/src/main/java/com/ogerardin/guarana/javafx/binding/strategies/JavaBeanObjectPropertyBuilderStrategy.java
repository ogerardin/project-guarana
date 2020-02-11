package com.ogerardin.guarana.javafx.binding.strategies;

import com.ogerardin.guarana.core.metamodel.PropertyInformation;
import com.ogerardin.guarana.javafx.binding.BindingStrategy;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.beans.property.Property;
import javafx.beans.property.adapter.JavaBeanObjectProperty;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JavaBeanObjectPropertyBuilderStrategy implements BindingStrategy {
    @Override
    public <C, P> void bind(C object, JfxInstanceUI<P> propertyUi, PropertyInformation propertyInformation, P propertyValue) {
        final String propertyName = propertyInformation.getName();
        try {
            JavaBeanObjectProperty<?> jfxSyntheticProperty = JavaBeanObjectPropertyBuilder.create()
                    .bean(object)
                    .name(propertyName)
                    .build();

            //noinspection unchecked
            propertyUi.boundObjectProperty().bindBidirectional((Property<P>) jfxSyntheticProperty);

            // DEBUG: trace change events on both UI field and object property
            jfxSyntheticProperty.addListener((observable, oldValue, newValue) -> {
                log.debug("jfx property [" + propertyName + "] changed: " + oldValue + " --> " + newValue);
            });
            propertyUi.boundObjectProperty().addListener((observable, oldValue, newValue) -> {
                log.debug("object bound to property [" + propertyName + "] changed: " + oldValue + " --> " + newValue);
            });

        } catch (NoSuchMethodException e) {
            // This happens when we try to use JavaBeanObjectPropertyBuilder on a read-only property
            log.debug("bindBidirectional threw NoSuchMethodException (read-only property?): " + e.toString());
            throw new IllegalArgumentException();
        }

    }
}
