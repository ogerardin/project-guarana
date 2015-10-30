/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx;

import com.ogerardin.guarana.core.ui.*;
import com.ogerardin.guarana.javafx.ui.JfxClassUI;
import com.ogerardin.guarana.javafx.ui.JfxCollectionUI;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import com.ogerardin.guarana.javafx.ui.JfxMapUI;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;

/**
 * UIBuilder implementation for JavaFX. The chosen renderable type is {@link Parent} as it can be the root of a
 * {@link Node} tree and can be put inside a {@link Stage}
 * @author oge
 * @since 07/09/2015
 */
public enum JfxUiBuilder implements UIBuilder<Parent> {

    /**
     * Single instance
     */
    INSTANCE {
        @Override
        public ClassUI<Parent> buildClassUI(Class clazz) {
            return new JfxClassUI(clazz);
        }

        @Override
        public <C> InstanceUI<Parent, C> buildInstanceUI(Class<C> clazz) {
            return new JfxInstanceUI<>(clazz);
        }

        @Override
        public <C> CollectionUI<Parent, C> buildCollectionUi(Class<C> itemClass) {
            return new JfxCollectionUI<>(itemClass);
        }

        @Override
        public <K, V> MapUI<Parent, K, V> buildMapUI() {
            return new JfxMapUI<>();
        }
    }

}
