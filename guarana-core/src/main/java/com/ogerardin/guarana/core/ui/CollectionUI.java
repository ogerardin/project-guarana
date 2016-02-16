/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.ui;

import java.util.Collection;

/**
 * @param <R> type of the rendered UI
 * @param <T> type of collection items

 * @author oge
 * @since 08/09/2015
 */
public interface CollectionUI<R, T> extends Renderable<R> {

    void setTarget(Collection<? extends T> target);

}
