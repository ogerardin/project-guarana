/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui;

import com.ogerardin.guarana.core.ui.Renderable;

/**
 * Interface that all Java FX UI classes must implement.
 * For JavaFX, the target rendering type is {@link javafx.scene.Parent} because it is as per the Javadoc "the base class
 * for all nodes that have children in the scene graph".
 *
 * @author oge
 * @since 10/02/2016
 */
public interface JfxRenderable extends Renderable<javafx.scene.Parent> {
}
