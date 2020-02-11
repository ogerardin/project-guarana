package com.ogerardin.guarana.javafx.binding.strategies;

import com.ogerardin.guarana.core.metamodel.PropertyInformation;
import com.ogerardin.guarana.javafx.binding.BindingStrategy;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.beans.property.Property;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;

@Slf4j
public class JfxBindingStrategy implements BindingStrategy {

    @Override
    public <C, P> void bind(C object, JfxInstanceUI<P> propertyUi, PropertyInformation propertyInformation, P propertyValue) {
        if (propertyInformation.getJfxProperty() == null) {
            throw new IllegalArgumentException();
        }

        final String propertyName = propertyInformation.getName();
        Property<P> jfxProperty;
        try {
            //noinspection unchecked
            jfxProperty = (Property<P>) PropertyUtils.getSimpleProperty(object, propertyInformation.getJfxProperty().getName());
        } catch (Exception e) {
            log.error("failed to get value for JavaFX property " + propertyInformation.getJfxProperty().getName(), e);
            return;
        }
        propertyUi.boundObjectProperty().bindBidirectional(jfxProperty);

        // DEBUG: trace change events on both UI field and object property
        jfxProperty.addListener((observable, oldValue, newValue) -> {
            log.debug("jfx property [" + propertyName + "] changed: " + oldValue + " --> " + newValue);
        });
        propertyUi.boundObjectProperty().addListener((observable, oldValue, newValue) -> {
            log.debug("object bound to property [" + propertyName + "] changed: " + oldValue + " --> " + newValue);
        });
    }
}
