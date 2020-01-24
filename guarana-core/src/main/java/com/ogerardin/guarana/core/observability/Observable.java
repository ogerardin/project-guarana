package com.ogerardin.guarana.core.observability;

import java.beans.PropertyChangeListener;

/**
 * Simple Observable as an interface.
 * Similar to {@link javafx.beans.Observable} but we don't want to depend on JavaFX in the core module.
 */
public interface Observable {
    void addPropertyChangeListener(PropertyChangeListener listener);
    void removePropertyChangeListener(PropertyChangeListener listener);
}