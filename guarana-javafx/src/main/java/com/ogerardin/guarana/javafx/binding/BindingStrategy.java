package com.ogerardin.guarana.javafx.binding;

import com.ogerardin.guarana.core.metamodel.PropertyInformation;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;

public interface BindingStrategy {

    <C, P> void bind(C object, JfxInstanceUI<P> propertyUi, PropertyInformation propertyInformation, P propertyValue);
}
