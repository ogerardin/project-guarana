/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.ui;

/**
 * @author oge
 * @since 08/09/2015
 */
public interface InstanceUI<R, T> extends Renderable<R> {

    void setTarget(T target);

}
