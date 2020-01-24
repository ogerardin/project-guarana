package com.ogerardin.guarana.core.observability;

import java.beans.PropertyChangeListener;

public interface Observable {
    void addPropertyChangeListener(PropertyChangeListener listener);
    void removePropertyChangeListener(PropertyChangeListener listener);
}