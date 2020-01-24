package com.ogerardin.guarana.core.test;

import com.ogerardin.guarana.core.observability.Observable;
import com.ogerardin.guarana.core.observability.ObservableFactory;
import com.ogerardin.guarana.core.test.domain.ItemPojo;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ObservabilityTest {

    @Test
    public void testObservableWrapper() {
        ItemPojo pojo = new ItemPojo();
        pojo.setName("value0");

        ItemPojo observablePojo = ObservableFactory.createObservable(pojo);

        PropertyChangeListener listener = mock(PropertyChangeListener.class);
        ((Observable)observablePojo).addPropertyChangeListener(listener);

        observablePojo.setName("value1");

        ArgumentCaptor<PropertyChangeEvent> argument = ArgumentCaptor.forClass(PropertyChangeEvent.class);
        verify(listener).propertyChange(argument.capture());
        assertEquals("name", argument.getValue().getPropertyName());
        assertEquals("value0", argument.getValue().getOldValue());
        assertEquals("value1", argument.getValue().getNewValue());
    }
}
