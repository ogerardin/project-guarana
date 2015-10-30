/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.ui;

import java.util.Map;

/**
 * @author oge
 * @since 08/09/2015
 */
public interface MapUI<R, K, V> extends Renderable<R> {

    void setTarget(Map<K, V> target);

}
