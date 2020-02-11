package com.ogerardin.guarana.javafx.binding.strategies;

import com.ogerardin.guarana.core.metamodel.PropertyInformation;
import com.ogerardin.guarana.javafx.binding.BindingStrategy;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JfxObservableListBindingStrategy implements BindingStrategy {
    @Override
    public <C, P> void bind(C object, JfxInstanceUI<P> propertyUi, PropertyInformation propertyInformation, P propertyValue) {
        if (! (propertyValue instanceof ObservableList)) {
            throw new IllegalArgumentException();
        }
        final String propertyName = propertyInformation.getName();
        //noinspection unchecked
        ObservableList<Object> observableList = (ObservableList<Object>) propertyValue;
        observableList.addListener((ListChangeListener<Object>) c -> {
            log.debug("Property [{}]: list changed: {}", propertyName, c);
            propertyUi.display(propertyValue);
        });
    }

}
