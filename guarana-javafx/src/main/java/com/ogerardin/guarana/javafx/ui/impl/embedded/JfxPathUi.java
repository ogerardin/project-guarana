/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui.impl.embedded;

import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Parent;
import javafx.scene.control.ComboBoxBase;

import java.nio.file.Path;

/**
 * Suitable for using as en embedded UI for Path properties as follows:
 * <pre>
 *             config.getClassInformation(Path.class).setEmbeddedUiClass(JfxPathUi.class);
 * </pre>
 *
 * @author olivier
 * @since 12/01/2016.
 */
public class JfxPathUi extends ComboBoxBase<Path> implements JfxInstanceUI<Path> {

    public JfxPathUi() {
    }

    @Override
    public Parent getRendered() {
        return this;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        setEditable(!readOnly);
    }

    @Override
    public ObjectProperty<Path> boundObjectProperty() {
        return valueProperty();
    }

    @Override
    public void bind(Path object) {
        setValue(object);
    }

}
