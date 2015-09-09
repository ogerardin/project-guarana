package com.ogerardin.guarana.javafx.ui.builder;

import com.ogerardin.guarana.core.ui.ClassUI;
import com.ogerardin.guarana.core.ui.InstanceUI;
import com.ogerardin.guarana.core.ui.UIBuilder;
import javafx.scene.Parent;

/**
 * Created by oge on 07/09/2015.
 */
public enum JfxUiBuilder implements UIBuilder<Parent> {

    INSTANCE {
        @Override
        public ClassUI<Parent> buildClassUI(Class clazz) {
            return new JfxClassUI(clazz);
        }

        @Override
        public <C> InstanceUI<Parent, C> buildInstanceUI(Class<C> clazz) {
            return new JfxInstanceUI(clazz);
        }
    }
}
