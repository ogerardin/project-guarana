/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui;

import com.ogerardin.guarana.core.ui.MapUI;
import javafx.scene.Parent;

/**
 * @author oge
 * @since 10/02/2016
 */
public interface JfxMapUI<K, V> extends MapUI<Parent, K, V>, JfxRenderable {
}
