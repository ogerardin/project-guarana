package com.ogerardin.guarana.javafx.binding;

import com.ogerardin.guarana.core.metamodel.PropertyInformation;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;

/**
 * Strategy interface for binding UI components to model properties.
 * Implementations define how to synchronize data between the model and the UI.
 *
 * @author Olivier Gérardin
 * @since 1.0
 */
public interface BindingStrategy {

    /**
     * Binds the specified property UI to the model object's property.
     *
     * @param object the model object containing the property
     * @param propertyUi the UI component for the property
     * @param propertyInformation metadata about the property
     * @param propertyValue the current value of the property
     */
    <C, P> void bind(C object, JfxInstanceUI<P> propertyUi, PropertyInformation propertyInformation, P propertyValue);
}
