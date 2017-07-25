/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.ui;

import java.util.Collection;

/**
 * A UI for interacting with a collection of items.
 *
 * @param <R> type of the rendered UI
 * @param <C> common type of collection items

 * @author oge
 * @since 08/09/2015
 */
public interface CollectionUI<R, C> extends Renderable<R> {

    void bind(Collection<? extends C> collection);

}
