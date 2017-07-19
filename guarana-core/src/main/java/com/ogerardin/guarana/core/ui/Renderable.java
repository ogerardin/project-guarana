/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.ui;

/**
 * An abstract UI object that may be rendered as concrete UI of type R
 *
 * @param <R> the concrete UI type
 *
 * @author oge
 * @since 08/09/2015
 */
public interface Renderable<R> {

    R getRendering();

    void setReadOnly(boolean readOnly);
}
