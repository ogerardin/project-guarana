/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui;

import com.ogerardin.guarana.core.registry.Identifier;
import com.ogerardin.guarana.core.registry.ObjectRegistry;
import javafx.geometry.Insets;
import javafx.scene.input.DataFormat;

/**
 * @author Olivier
 * @since 05/06/15
 */
public interface Const {

    Insets DEFAULT_INSETS = new Insets(25, 25, 25, 25);

    /**
     * Custom clipboard format for drag-and-drop.
     * Associated content is expected to be an {@link Identifier} that is the key to the source object in the
     * {@link ObjectRegistry}.
     */
    DataFormat DATA_FORMAT_OBJECT_IDENTIFIER = new DataFormat("application/x.object-reference");
}
