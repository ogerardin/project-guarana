/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui;

import java.lang.reflect.InvocationTargetException;

/**
 * @author olivier
 * @since 13/01/2016.
 */
public interface ValueSetter {

    void setValue(Object value) throws InvocationTargetException, IllegalAccessException;
}
