/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui;

import com.ogerardin.guarana.core.ui.UIBuilder;

/**
 * Specialized {@link UIBuilder} for JavaFX.
 * Renders UI instances as {@link javafx.scene.Parent}, as it can be the root of a
 * {@link javafx.scene.Node} tree and can be put inside a {@link javafx.stage.Stage}
 *
 * @author oge
 * @since 10/02/2016
 */
public interface JfxUIBuilder extends UIBuilder<javafx.scene.Parent> {
}
