/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.ui;

import java.util.Map;

/**
 * @param <R> type of the rendered UI
 * @param <K> type of the map keys
 * @param <V> type of the map values

 * @author oge
 * @since 08/09/2015
 */
public interface MapUI<R, K, V> extends Renderable<R> {

    void setTarget(Map<K, V> target);

}
