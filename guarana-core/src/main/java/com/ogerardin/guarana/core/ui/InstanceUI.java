/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.ui;

/**
 * A UI for a interacting with a single instance
 *
 * @param <R> type of the rendered UI object
 * @param <T> type of the object being represented
 *
 * @author oge
 * @since 08/09/2015
 */
public interface InstanceUI<R, T> extends Renderable<R> {

    void bind(T object);

}
