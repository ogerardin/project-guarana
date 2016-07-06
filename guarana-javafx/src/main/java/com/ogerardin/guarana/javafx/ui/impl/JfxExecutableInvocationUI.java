/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui.impl;

import com.ogerardin.guarana.core.config.Configuration;
import com.ogerardin.guarana.core.introspection.Introspector;
import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import com.ogerardin.guarana.javafx.ui.JfxRenderable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

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
 * @param <R> the result type (same as C for constructors)
 *
 * @author Olivier
 * @since 05/06/15
 */
public class JfxExecutableInvocationUI<C, R> extends JfxForm implements JfxRenderable {

    private List<JfxInstanceUI> fieldUiList = new ArrayList<>();

    /**
     * {@link Consumer#accept} will be called with the result of the method call / constructor
     * in case of success
     */
    private Consumer<R> onSuccess = null;

    /**
     * For a method call, the target object on which to invoke the method.
     * Not relevant for a constructor.
     */
    private ObjectProperty<C> targetProperty = new SimpleObjectProperty<>();


    public JfxExecutableInvocationUI(JfxUiManager builder, Constructor<C> constructor) {
        //noinspection unchecked
        this(builder, constructor, constructor.getDeclaringClass(), (Class<R>) constructor.getDeclaringClass());
    }

    public JfxExecutableInvocationUI(JfxUiManager builder, Method method) {
        //noinspection unchecked
        this(builder, method, (Class<C>) method.getDeclaringClass(), (Class<R>) method.getReturnType());
    }

    private JfxExecutableInvocationUI(JfxUiManager builder, Executable executable, Class<C> declaringClass, Class<R> resultType) {
        super(builder);
        buildUi(executable);
    }

    private void buildUi(Executable executable) {
        final String title = Configuration.humanize(executable.getName());
        addTitle(title);

        // build params list
        GridPane grid = buildGridPane();
        root.getChildren().add(grid);
        int row = 0;

        final Parameter[] parameters = executable.getParameters();
        final Type[] genericParameterTypes = executable.getGenericParameterTypes();

        for (int i = 0, parametersLength = parameters.length; i < parametersLength; i++) {
            final Parameter param = parameters[i];
            final Type genericParamType = genericParameterTypes[i];
            final String paramName = param.getName();
            final Class<?> paramType = param.getType();

            // label
            final String humanizedName = Configuration.humanize(paramName);
            Label label = new Label(humanizedName);
            grid.add(label, 0, row);

            // field
            final Node field = buildParamUi(paramType);
            grid.add(field, 1, row);
            if (row == 0) {
                field.requestFocus();
            }

            // if it's a collection, add a button to open as list
            if (Collection.class.isAssignableFrom(paramType)) {
                Button zoomButton = new Button("(+)");
                zoomButton.setOnAction(e -> zoomCollection(zoomButton, genericParamType));
                grid.add(zoomButton, 2, row);
            }


            row++;
        }

        Button goButton = new Button(executable instanceof Constructor ? "Create" : "Go");
        goButton.setOnAction(event -> {
            try {
                List paramValues = getParamValues();
                final Object[] values = paramValues.toArray();
                if (executable instanceof Constructor) {
                    final Constructor<C> constructor = (Constructor<C>) executable;
                    invokeConstructor(constructor, values, onSuccess);
                } else if (executable instanceof Method) {
                    final Method method = (Method) executable;
                    invokeMethod(method, values, onSuccess);
                }
                getBuilder().hide(this);
            } catch (Exception e) {
                getBuilder().displayException(e);
            }
        });
        root.getChildren().add(goButton);
    }

    private void invokeMethod(Method method, Object[] params, Consumer<R> onSuccess) throws IllegalAccessException, InvocationTargetException {
        //FIXME that won't work if the method is a "related" method (referencing the target type C but not exposed by it)
        // in that case we need to find a way to provide/select the instance on which to invoke the method
        R result = (R) method.invoke(getTarget(), params);
        if (onSuccess != null) {
            onSuccess.accept(result);
        }
    }

    private void invokeConstructor(Constructor<C> constructor, Object[] params, Consumer<R> onSuccess) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        C instance = constructor.newInstance(params);
        if (onSuccess != null) {
            onSuccess.accept((R) instance);
        }
    }

    private <T> Node buildParamUi(Class<T> paramType) {
        //TextField field = new TextField();
        //final StringConverter stringConverter = Bindings.getStringConverter(paramType, getConfiguration());
        //field.textProperty().bindBidirectional(jfxProperty, stringConverter);
        final JfxInstanceUI<T> ui = getBuilder().buildEmbeddedInstanceUI(paramType);
        final Node field = ui.getRendering();

        // configure the field as a target for drag and drop
        configureDropTarget(field,
                (T value) -> paramType.isAssignableFrom(value.getClass()),
                value -> ui.targetProperty().setValue(value)
        );

        fieldUiList.add(ui);

        return field;
    }

    private <T> void zoomCollection(Node parent, Type genericParamtype) {
        final Class<T> itemType = Introspector.getSingleParameterType(genericParamtype);
        final Collection<T> collection = new ArrayList<>();
        getBuilder().displayCollection(collection, itemType, parent, "Collection parameter");
    }

    private List<Object> getParamValues() {
        return fieldUiList.stream()
                .map(ui -> ui.targetProperty().getValue())
                .collect(Collectors.toList());
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
