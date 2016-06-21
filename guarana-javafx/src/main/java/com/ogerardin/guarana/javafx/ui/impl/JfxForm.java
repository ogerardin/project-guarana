/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui.impl;

import com.ogerardin.guarana.javafx.JfxUiManager;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * @author oge
 * @since 21/06/2016
 */
public abstract class JfxForm extends JfxUI {

    protected final VBox root;


    public JfxForm(JfxUiManager builder) {
        super(builder);

        this.root = new VBox();
    }

    protected GridPane buildGridPane() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(Const.DEFAULT_INSETS);

        ColumnConstraints column2 = new ColumnConstraints();
        column2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().setAll(new ColumnConstraints(), column2); // second column gets any extra width
        return grid;
    }

    protected Label addTitle(String title) {
        final Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        titleLabel.setGraphic(new ImageView(ICON_DRAG_HANDLE));
        root.getChildren().add(titleLabel);
        return titleLabel;
    }

}