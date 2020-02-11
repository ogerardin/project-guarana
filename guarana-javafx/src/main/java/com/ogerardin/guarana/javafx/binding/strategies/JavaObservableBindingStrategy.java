package com.ogerardin.guarana.javafx.binding.strategies;

import com.ogerardin.guarana.core.metamodel.PropertyInformation;
import com.ogerardin.guarana.javafx.binding.BindingStrategy;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import lombok.extern.slf4j.Slf4j;

import java.util.Observable;
import java.util.Observer;

@Slf4j
public class JavaObservableBindingStrategy implements BindingStrategy {

    @Override
    public <C, P> void bind(C object, JfxInstanceUI<P> propertyUi, PropertyInformation propertyInformation, P propertyValue) {
        if (! (propertyValue instanceof Observable)) {
            throw new IllegalArgumentException();
        }
        final String propertyName = propertyInformation.getName();
        Observable observableValue = (Observable) propertyValue;
        Observer observer = (observable, o) -> propertyUi.boundObjectProperty().setValue(propertyValue);
        observableValue.addObserver(observer);
        observer.update(observableValue, this);
    }
}
