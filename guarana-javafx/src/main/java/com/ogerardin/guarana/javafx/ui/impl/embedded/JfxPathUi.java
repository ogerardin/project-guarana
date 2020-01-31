/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui.impl.embedded;

import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.utils.FontAwesomeIconFactory;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Suitable for using as en embedded UI for Path properties as follows:
 * <pre>
 *             config.getClassInformation(Path.class).setEmbeddedUiClass(JfxPathUi.class);
 * </pre>
 *
 * @author olivier
 * @since 12/01/2016.
 */
public class JfxPathUi extends HBox implements JfxInstanceUI<Path> {

    private final ObjectProperty<Path> boundPathProperty = new SimpleObjectProperty<>();

    private final Button button;
    private final TextField text;

    public JfxPathUi() {
        this.text = new TextField();
        getChildren().add(this.text);

        button = FontAwesomeIconFactory.get().createIconButton(FontAwesomeIcon.FOLDER);
        button.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                boundPathProperty.setValue(file.toPath());
            }
        });
        getChildren().add(button);
        setHgrow(text, Priority.ALWAYS);

        text.textProperty().bindBidirectional(boundObjectProperty(), new PathConverter());
    }

    @Override
    public Parent getRendered() {
        return this;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        text.setEditable(!readOnly);
        button.disableProperty().set(readOnly);
    }

    @Override
    public ObjectProperty<Path> boundObjectProperty() {
        return boundPathProperty;
    }

    @Override
    public void bind(Path path) {
        boundPathProperty.setValue(path);
    }

    @Override
    public void display(Path object) {
        boundObjectProperty().set(object);
    }

    private class PathConverter extends StringConverter<Path> {
        @Override
        public String toString(Path path) {
            return (path == null) ? "" : path.toString();
        }

        @Override
        public Path fromString(String string) {
            return Paths.get(string);
        }
    }
}
