/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.ui;

/*
 * Created by oge on 08/09/2015.
 */

/**
 * An abstract UI object that may be rendered as concrete UI type R
 * @param <R> the concrete UI type
 */
public interface Renderable<R> {

    R render();
}
