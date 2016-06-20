/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui.impl;

import com.ogerardin.guarana.core.introspection.Introspector;
import com.ogerardin.guarana.javafx.JfxUiManager;
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

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A UI for calling a specific method or constructor.
 *
 * @param <C> the declaring class of the method/constructor
 *
 * @author Olivier
 * @since 05/06/15
 */
public class JfxExecutableInvocationUI<C> extends JfxUI implements JfxRenderable {

    private final Parent root;

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


    public JfxExecutableInvocationUI(JfxUiManager builder, Executable executable) {
        super(builder);

        root = buildUi(executable);
    }

    private Parent buildUi(Executable executable) {
        VBox root = new VBox();
        final Label title = new Label(Introspector.humanize(executable.getName()));
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        title.setTooltip(new Tooltip(executable.toGenericString()));
        root.getChildren().add(title);

        // build params list
        GridPane grid = buildGridPane();
        root.getChildren().add(grid);
        int row = 0;

        final Parameter[] parameters = executable.getParameters();
        final Type[] genericParameterTypes = executable.getGenericParameterTypes();

        for (int i = 0, parametersLength = parameters.length; i < parametersLength; i++) {
            final Parameter param = parameters[i];
            final Type genericParamType = genericParameterTypes[i];

            buildPropertyUi(grid, row, param, genericParamType);
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
        return root;
    }

    private <T> void buildPropertyUi(GridPane grid, int row, Parameter param, Type genericParamType) {
        final Class<T> paramType = (Class<T>) param.getType();
        final String paramName = param.getName();

        final String humanizedName = Introspector.humanize(paramName);
        Label label = new Label(humanizedName);
        grid.add(label, 0, row);

        //TextField field = new TextField();
        //final StringConverter stringConverter = Bindings.getStringConverter(paramType, getConfiguration());
        //field.textProperty().bindBidirectional(jfxProperty, stringConverter);
        final JfxInstanceUI<T> ui = getEmbeddedInstanceUI(paramType);
        paramFieldUiList.add(ui);
        final Node field = ui.render();
        grid.add(field, 1, row);
        if (row == 0) {
            field.requestFocus();
        }

        // if it's a collection, add a button to open as list
        if (Collection.class.isAssignableFrom(paramType)) {
            Button button = new Button("(+)");
            button.setOnAction(e -> zoomCollection(button, genericParamType));
            grid.add(button, 2, row);
        }

        // configure the field as a target for drag and drop
        configureDropTarget(field,
                (T value) -> paramType.isAssignableFrom(value.getClass()),
                value -> ui.targetProperty().setValue(value)
        );
    }

    private <T> JfxInstanceUI<T> getEmbeddedInstanceUI(Class<T> paramType) {
        return getBuilder().buildEmbeddedInstanceUI(paramType);
    }

    private <T> void zoomCollection(Node parent, Type genericParamtype) {
        final Class<T> itemType = Introspector.getSingleParameterType(genericParamtype);
        final Collection<T> collection = new ArrayList<>();
        getBuilder().displayCollection(collection, itemType, parent, "Collection parameter");
    }


    @NotNull
    private GridPane buildGridPane() {
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
