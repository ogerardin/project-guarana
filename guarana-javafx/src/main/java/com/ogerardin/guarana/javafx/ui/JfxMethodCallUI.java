/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui;

import com.ogerardin.guarana.core.introspection.Introspector;
import com.ogerardin.guarana.core.ui.CollectionUI;
import com.ogerardin.guarana.core.ui.Renderable;
import com.ogerardin.guarana.javafx.JfxUiBuilder;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Olivier
 * @since 05/06/15
 */
public class JfxMethodCallUI extends JfxUI implements Renderable<Parent> {

    private final VBox root;
    private final Executable executable;

    public JfxMethodCallUI(JfxUiBuilder builder, Executable executable) {
        super(builder);
        this.executable = executable;

        root = new VBox();
        final Label title = new Label(Introspector.humanize(executable.getName()));
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        title.setTooltip(new Tooltip(executable.toGenericString()));
        root.getChildren().add(title);

        // build params list
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(Const.DEFAULT_INSETS);

        ColumnConstraints column2 = new ColumnConstraints();
        column2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().setAll(new ColumnConstraints(), column2); // second column gets any extra width

        root.getChildren().add(grid);
        int row = 0;

        Map<String, Object> params = new HashMap<>();
        for (Parameter param : executable.getParameters()) {

            params.put(param.getName(), null);

            final String humanizedName = Introspector.humanize(param.getName());
            Label label = new Label(humanizedName);
            grid.add(label, 0, row);

            TextField field = new TextField();
            grid.add(field, 1, row);
            if (row == 0) {
                field.requestFocus();
            }

            // if it's a collection, add a button to open as list
            if (Collection.class.isAssignableFrom(param.getType())) {
                Button button = new Button("...");
                button.setOnAction(e -> {
                    CollectionUI<Parent, Object> collectionUI = getBuilder().buildCollectionUi(Object.class);
                    collectionUI.setTarget(new ArrayList<>());
                    JfxUiBuilder.display(collectionUI, button, "Collection parameter");
                });
                grid.add(button, 2, row);
            }

            // TODO set the field as a target for drag and drop

            row++;
        }

        Button go = new Button(executable instanceof Constructor ? "Create" : "Go");
        go.setOnAction(event -> {
            // TODO: gather params and invoke method
        });
        root.getChildren().add(go);
    }

    @Override
    public Parent render() {
        return root;
    }
}
