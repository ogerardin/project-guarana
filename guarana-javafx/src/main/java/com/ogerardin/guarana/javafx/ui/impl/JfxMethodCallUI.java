/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui.impl;

import com.ogerardin.guarana.core.config.ClassConfiguration;
import com.ogerardin.guarana.core.introspection.Introspector;
import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.ui.JfxCollectionUI;
import com.ogerardin.guarana.javafx.ui.JfxRenderable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
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
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Consumer;

/**
 * A UI for calling a specific method or constructor.
 *
 * @author Olivier
 * @since 05/06/15
 */
public class JfxMethodCallUI extends JfxUI implements JfxRenderable {

    private final VBox root;
    private final Map<String, Property> params;
    private Consumer onSuccess = null;

    public JfxMethodCallUI(JfxUiManager builder, Executable executable) {
        super(builder);

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

        params = new HashMap<>();
        for (Parameter param : executable.getParameters()) {

            final SimpleStringProperty jfxProperty = new SimpleStringProperty();
            params.put(param.getName(), jfxProperty);

            final String humanizedName = Introspector.humanize(param.getName());
            Label label = new Label(humanizedName);
            grid.add(label, 0, row);

            TextField field = new TextField();
            field.textProperty().bindBidirectional(jfxProperty);

            grid.add(field, 1, row);
            if (row == 0) {
                field.requestFocus();
            }

            // if it's a collection, add a button to open as list
            if (Collection.class.isAssignableFrom(param.getType())) {
                Button button = new Button("...");
                button.setOnAction(e -> {
                    JfxCollectionUI<Object> collectionUI = getBuilder().buildCollectionUi(Object.class);
                    collectionUI.setTarget(new ArrayList<>());
                    getBuilder().display(collectionUI, button, "Collection parameter");
                });
                grid.add(button, 2, row);
            }

            // set the field as a target for drag and drop
            configureDropTarget(field,
                    value -> param.getType().isAssignableFrom(value.getClass()),
                    value -> {
                        //params.put(param.getName(), value);
                        ClassConfiguration classConfig = getConfiguration().forClass(value.getClass());
                        //FIXME set param value, not just field text ! (see DefaultJfxInstanceUI)
                        field.setText(classConfig.toString(value));
                    });

            row++;
        }

        Button goButton = new Button(executable instanceof Constructor ? "Create" : "Go");
        goButton.setOnAction(event -> {
            try {
                List paramValues = getParamValues(executable);
                if (executable instanceof Constructor) {
                    Object instance = ((Constructor) executable).newInstance(paramValues.toArray());
                    if (onSuccess != null) {
                        onSuccess.accept(instance);
                    }
                } else if (executable instanceof Method) {
                    Object result = ((Method) executable).invoke(paramValues);
                    if (onSuccess != null) {
                        onSuccess.accept(result);
                    }
                }
                getBuilder().hide(this);
            } catch (Exception e) {
                getBuilder().displayException(e);
            }
        });
        root.getChildren().add(goButton);
    }

    private List getParamValues(Executable executable) {
        List paramValues = new ArrayList();
        for (Parameter param : executable.getParameters()) {
            Property property = params.get(param.getName());
            Object paramValue = property.getValue();
            paramValues.add(paramValue);
        }
        return paramValues;
    }

    @Override
    public Parent render() {
        return root;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        throw new UnsupportedOperationException();
    }

    public void setOnSuccess(Consumer handler) {
        this.onSuccess = handler;
    }

}
