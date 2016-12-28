/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui.impl;

import com.ogerardin.guarana.core.config.Configuration;
import com.ogerardin.guarana.core.introspection.Introspector;
import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import com.ogerardin.guarana.javafx.ui.JfxRenderable;
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

    private final Class<C> declaringClass;
    private final Class<R> resultType;
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
    private Node targetField;
    private JfxInstanceUI<C> targetUi;


    JfxExecutableInvocationUI(JfxUiManager builder, Constructor<C> constructor) {
        //noinspection unchecked
        this(builder, constructor, constructor.getDeclaringClass(), (Class<R>) constructor.getDeclaringClass());
    }

    JfxExecutableInvocationUI(JfxUiManager builder, Method method) {
        //noinspection unchecked
        this(builder, method, (Class<C>) method.getDeclaringClass(), (Class<R>) method.getReturnType());
    }

    private JfxExecutableInvocationUI(JfxUiManager builder, Executable executable,
                                      Class<C> declaringClass, Class<R> resultType) {
        super(builder);
        this.declaringClass = declaringClass;
        this.resultType = resultType;
        buildUi(executable);
    }

    private void buildUi(Executable executable) {
        final String title = Configuration.humanize(executable.getName());
        addTitle(title);

        // build params grid
        GridPane grid = buildGridPane();
        root.getChildren().add(grid);
        int row = 0;

        // if invoking a method, the first field is the target object
        if (executable instanceof Method) {
            Label label = new Label(declaringClass.getSimpleName());
            grid.add(label, 0, row);

            targetUi = getBuilder().buildEmbeddedInstanceUI(declaringClass);
            targetField = targetUi.getRendering();
            label.setLabelFor(targetField);

            configureDropTarget(targetField,
                    (C value) -> declaringClass.isAssignableFrom(value.getClass()),
                    value -> targetUi.boundObjectProperty().setValue(value)
            );
            grid.add(targetField, 1, row);

            row++;
        }

        // rest of the parameters
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
            label.setLabelFor(field);

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
            doInvoke(executable);
        });
        root.getChildren().add(goButton);
    }

    private void doInvoke(Executable executable) {
        try {
            final Object[] paramValues = getParamValues();

            if (executable instanceof Constructor) {
                final Constructor<C> constructor = (Constructor<C>) executable;
                doInvokeConstructor(constructor, paramValues, onSuccess);
            } else if (executable instanceof Method) {
                final Method method = (Method) executable;
                doInvokeMethod(method, getTargetValue(), paramValues, onSuccess);
            }
            getBuilder().hide(this);
        } catch (Exception e) {
            getBuilder().displayException(e);
        }
    }

    private C getTargetValue() {
        return targetUi.boundObjectProperty().getValue();
    }

    private static <R> void doInvokeMethod(Method method, Object target, Object[] params, Consumer<R> onSuccess) throws IllegalAccessException, InvocationTargetException {
        R result = (R) method.invoke(target, params);
        if (onSuccess != null) {
            onSuccess.accept(result);
        }
    }

    private static <C, R> void doInvokeConstructor(Constructor<C> constructor, Object[] params, Consumer<R> onSuccess) throws InstantiationException, IllegalAccessException, InvocationTargetException {
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
        fieldUiList.add(ui);

        final Node field = ui.getRendering();

        // configure the field as a target for drag and drop
        configureDropTarget(field,
                (T value) -> paramType.isAssignableFrom(value.getClass()),
                value -> ui.boundObjectProperty().setValue(value)
        );

        return field;
    }

    private <T> void zoomCollection(Node parent, Type genericParamtype) {
        final Class<T> itemType = Introspector.getSingleParameterType(genericParamtype);
        final Collection<T> collection = new ArrayList<>();
        getBuilder().displayCollection(collection, itemType, parent, "Collection parameter");
    }

    private Object[] getParamValues() {
        final List<Object> paramValueList = fieldUiList.stream()
                .map(ui -> ui.boundObjectProperty().getValue())
                .collect(Collectors.toList());
        return paramValueList.toArray();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        throw new UnsupportedOperationException();
    }

    public void setOnSuccess(Consumer handler) {
        this.onSuccess = handler;
    }


    public void setContext(Object context) {
        if (declaringClass.isAssignableFrom(context.getClass())) {
            // the context is of the declaring class, use it as target
            targetUi.boundObjectProperty().setValue((C) context);
            targetUi.setReadOnly(true);
        } else {
            //TODO the target is not of the declaring class, what should we do ?
            // (At least one of the params should be assignable from the target)
            targetUi.boundObjectProperty().setValue(null);
            targetUi.setReadOnly(false);
        }
    }


}
