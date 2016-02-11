/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui.impl;

import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.scene.control.TextField;

/**
 * An implementation of JfxInstanceUI suited for use as an embedded field.
 *
 * @author olivier
 * @since 11/02/2016.
 */
public class DefaultJfxEmbeddedInstanceUI<T> extends TextField implements JfxInstanceUI<T> {

    private final JfxUiManager jfxUiManager;
    private final Class<T> clazz;

    public DefaultJfxEmbeddedInstanceUI(JfxUiManager jfxUiManager, Class<T> clazz) {
        this.jfxUiManager = jfxUiManager;
        this.clazz = clazz;
    }

    @Override
    public void setTarget(T target) {
        //FIXME temporary
        String string = jfxUiManager.getConfiguration().forClass(clazz).toString(target);
        setText(string);
    }

    @Override
    public TextField render() {
        return this;
    }
}
