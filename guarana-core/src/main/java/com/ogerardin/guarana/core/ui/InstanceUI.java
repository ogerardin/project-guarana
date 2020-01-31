/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.ui;

/**
 * A UI for a interacting with a single instance
 *
 * @param <R> type of the rendered UI object
 * @param <C> type of the object being represented
 *
 * @author oge
 * @since 08/09/2015
 */
public interface InstanceUI<R, C> extends Renderable<R> {

    void bind(C object);

    void display(C object);

}
