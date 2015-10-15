/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui;

import javafx.geometry.Insets;
import javafx.scene.input.DataFormat;

/**
 * Created by Olivier on 05/06/15.
 */
public interface Const {

    Insets DEFAULT_INSETS = new Insets(25, 25, 25, 25);

    // custom clipboard format for drag-and-drop
    DataFormat DATA_FORMAT_OBJECT_REFERENCE = new DataFormat("com.ogerardin.guarana.core.ObjectReference");
}
