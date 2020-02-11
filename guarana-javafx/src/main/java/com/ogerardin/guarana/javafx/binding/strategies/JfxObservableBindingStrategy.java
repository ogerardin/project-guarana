package com.ogerardin.guarana.javafx.binding.strategies;

import com.ogerardin.guarana.core.metamodel.PropertyInformation;
import com.ogerardin.guarana.javafx.binding.BindingStrategy;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JfxObservableBindingStrategy implements BindingStrategy {
    @Override
    public <C, P> void bind(C object, JfxInstanceUI<P> propertyUi, PropertyInformation propertyInformation, P propertyValue) {
        if (! (propertyValue instanceof Observable)) {
            throw new IllegalArgumentException();
        }
        final String propertyName = propertyInformation.getName();
        InvalidationListener listener = observable -> propertyUi.boundObjectProperty().setValue(propertyValue);
        Observable observableValue = (Observable) propertyValue;
        observableValue.addListener(listener);
        //FIXME listener is not called when list is changed subsequently; likely because change events are not invalidation events
        listener.invalidated(observableValue);
    }
}
