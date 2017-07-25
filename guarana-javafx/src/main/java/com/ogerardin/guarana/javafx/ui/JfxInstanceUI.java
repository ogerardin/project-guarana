/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui;

import com.ogerardin.guarana.core.ui.InstanceUI;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Parent;

/**
 * @author oge
 * @since 10/02/2016
 */
public interface JfxInstanceUI<C> extends InstanceUI<Parent, C>, JfxRenderable {

    /**
     * The JavaFX-style property that encapsulates the target object for this UI
     */
    ObjectProperty<C> boundObjectProperty();
}
