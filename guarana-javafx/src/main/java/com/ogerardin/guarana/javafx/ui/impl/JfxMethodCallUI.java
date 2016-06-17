/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui.impl;

import com.ogerardin.guarana.core.introspection.Introspector;
import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.ui.JfxCollectionUI;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import com.ogerardin.guarana.javafx.ui.JfxRenderable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A UI for calling a specific method or constructor.
 *
 * @param <C> the declaring class of the method/constructor
 *
 * @author Olivier
 * @since 05/06/15
 */
public class JfxMethodCallUI<C> extends JfxUI implements JfxRenderable {

    private final VBox root;

    //private final Map<String, Property> paramNameToProperty;
    private List<JfxInstanceUI> paramFieldUiList = new ArrayList<>();

    /**
     * {@link Consumer#accept} will be called with the result of the method call / constructor
     * in case of success
     */
    private Consumer onSuccess = null;

    /**
     * For a method call, the target object on which to invoke the method.
     * Not relevant for a constructor.
     */
    private ObjectProperty<C> targetProperty = new SimpleObjectProperty<>();


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

        //paramNameToProperty = new HashMap<>();
        for (Parameter param : executable.getParameters()) {
            final Class<?> paramType = param.getType();
            //SimpleObjectProperty jfxProperty = createSimpleObjectProperty(paramType);
            //paramNameToProperty.put(param.getName(), jfxProperty);

            final String humanizedName = Introspector.humanize(param.getName());
            Label label = new Label(humanizedName);
            grid.add(label, 0, row);

            //TextField field = new TextField();
            //final StringConverter stringConverter = Bindings.getStringConverter(paramType, getConfiguration());
            //field.textProperty().bindBidirectional(jfxProperty, stringConverter);
            final JfxInstanceUI<Object> ui = (JfxInstanceUI<Object>) getBuilder().buildEmbeddedInstanceUI(paramType);
            paramFieldUiList.add(ui);
            final Node field = ui.render();

            grid.add(field, 1, row);
            if (row == 0) {
                field.requestFocus();
            }

            // if it's a collection, add a button to open as list
            if (Collection.class.isAssignableFrom(paramType)) {
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
                    new Predicate<Object>() {
                        @Override
                        public boolean test(Object value) {
                            return paramType.isAssignableFrom(value.getClass());
                        }
                    },
                    //value -> jfxProperty.setValue(value));
                    new Consumer<Object>() {
                        @Override
                        public void accept(Object value) {
                            ui.targetProperty().setValue(value);
                        }
                    }
            );
            row++;
        }

        Button goButton = new Button(executable instanceof Constructor ? "Create" : "Go");
        goButton.setOnAction(event -> {
            try {
                List paramValues = getParamValues();
                final Object[] values = paramValues.toArray();
                if (executable instanceof Constructor) {
                    final Constructor<C> constructor = (Constructor<C>) executable;
                    C instance = constructor.newInstance(values);
                    if (onSuccess != null) {
                        onSuccess.accept(instance);
                    }
                } else if (executable instanceof Method) {
                    final Method method = (Method) executable;
                    Object result = method.invoke(getTarget(), values);
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

    @NotNull
    private <T> SimpleObjectProperty<T> createSimpleObjectProperty(Class<T> propertyClass) {
        return new SimpleObjectProperty<T>();
    }

    private List<Object> getParamValues() {
        return paramFieldUiList.stream()
                .map(ui -> ui.targetProperty().getValue())
                .collect(Collectors.toList());

//        return Arrays.stream(executable.getParameters())
//                .map(Parameter::getName)
//                .map(paramNameToProperty::get)
//                .map(Property::getValue)
//                .collect(Collectors.toList());
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

    protected C getTarget() {
        return targetProperty.get();
    }

    public void setTarget(C target) {
        targetProperty.setValue(target);
    }

    public ObjectProperty<C> targetProperty() {
        return targetProperty;
    }


}
