/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.ogerardin.guarana.core.config.Configuration;
import com.ogerardin.guarana.core.introspection.ClassInformation;
import com.ogerardin.guarana.core.introspection.Introspector;
import com.ogerardin.guarana.core.introspection.PropertyInformation;
import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.binding.Bindings;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Observer;

/**
 * Default implementation of a InstanceUI for JavaFX.
 *
 * @param <T> type of the object being represented
 *
 * @author Olivier
 * @since 29/05/15
 */
public class DefaultJfxInstanceUI<T> extends JfxForm implements JfxInstanceUI<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultJfxInstanceUI.class);

    private BiMap<Node, PropertyInformation> nodeToPropertyInformation = HashBiMap.create();
    private BiMap<JfxInstanceUI, PropertyInformation> uiToPropertyInformation = HashBiMap.create();

    private ObjectProperty<T> targetProperty = new SimpleObjectProperty<>();

    public DefaultJfxInstanceUI(JfxUiManager builder, Class<T> clazz) {
        super(builder);
        buildUi(clazz);
    }

    private void buildUi(Class<T> clazz) {

        ClassInformation<T> classInformation = Introspector.getClassInformation(clazz);

        // title
        final String displayName = getConfiguration().getClassDisplayName(clazz);
        final Label title = addTitle(displayName);
        configureDragSource(title, this::getTarget);
        configureContextMenu(title, classInformation, this::getTarget);

        // build properties form
        GridPane grid = buildGridPane();
        root.getChildren().add(grid);
        int row = 0;
        for (PropertyInformation propertyInformation : classInformation.getProperties()) {
            String propertyName = propertyInformation.getName();
            final Class<?> propertyType = propertyInformation.getPropertyType();
            final Method readMethod = propertyInformation.getReadMethod();

            // ignore hidden properties
            if (getConfiguration().isHiddenProperty(classInformation.getTargetClass(), propertyName)) {
                continue;
            }

            // label
            final String humanizedName = Configuration.humanize(propertyInformation.getDisplayName());
            Label label = new Label(humanizedName);
            label.setTooltip(new Tooltip(propertyInformation.toString()));
            grid.add(label, 0, row);

            // field
            final Node field = buildPropertyUi(propertyInformation, propertyType);
            grid.add(field, 1, row);
            if (row == 0) {
                field.requestFocus();
            }

            // if it's a collection, add a button to open as list
            if (Collection.class.isAssignableFrom(propertyType)) {
                Button zoomButton = new Button("(+)");
                zoomButton.setOnAction(e -> zoomCollection(zoomButton, readMethod, humanizedName));
                grid.add(zoomButton, 2, row);
            }
            // if it's an array, add a button to open as a list only if element type is not primitive
            else if (propertyType.isArray()) {
                final Class<?> componentType = propertyType.getComponentType();
                if (!componentType.isPrimitive()) {
                    Button zoomButton = new Button("[+]");
                    zoomButton.setOnAction(e -> zoomArray(zoomButton, readMethod, componentType, humanizedName));
                    grid.add(zoomButton, 2, row);
                }
            }
            // otherwise if it's not a primitive type, add a button to zoom on property as single instance
            else if (!propertyType.isPrimitive()) {
                Button zoomButton = new Button("...");
                zoomButton.setOnAction(e -> zoomProperty(zoomButton, propertyType, readMethod, humanizedName));
                grid.add(zoomButton, 2, row);
            }

            row++;
        }
    }


    private <T> Node buildPropertyUi(PropertyInformation propertyInformation, Class<T> propertyType) {

        JfxInstanceUI<T> ui = getBuilder().buildEmbeddedInstanceUI(propertyType);
        Node field = ui.getRendering();

        // set the field as a target for drag and drop
        configureDropTarget(field,
                (T value) -> propertyType.isAssignableFrom(value.getClass()),
                value -> ui.targetProperty().setValue(value));

        uiToPropertyInformation.put(ui, propertyInformation);
        nodeToPropertyInformation.put(field, propertyInformation);

        return field;
    }

    private <C> void zoomCollection(Node parent, Method readMethod, String title) {
        try {
            // Try to use generic introspection to determine the type of collection members.
            final Class<C> itemType = Introspector.getCollectionItemType(readMethod);
            final Collection<C> collection = (Collection<C>) readMethod.invoke(getTarget());
            getBuilder().displayCollection(collection, itemType, parent, title);
        } catch (Exception e) {
            getBuilder().displayException(e);
        }
    }

    private <C> void zoomArray(Node parent, Method readMethod, Class<C> itemType, String title) {
        try {
            final C[] array = (C[]) readMethod.invoke(getTarget());
            getBuilder().displayArray(array, itemType, parent, title);
        } catch (Exception e) {
            getBuilder().displayException(e);
        }
    }

    private <P> void zoomProperty(Node parent, Class<P> propertyType, Method readMethod, String title) {
        try {
            final P value = (P) readMethod.invoke(getTarget());
            getBuilder().displayInstance(value, propertyType, parent, title);
        } catch (Exception ex) {
            getBuilder().displayException(ex);
        }
    }


    public void setTarget(T target) {
        if (getTarget() != null) {
            unbind();
        }
        targetProperty.set(target);
        bind(target);
    }

    private void unbind() {
        for (Map.Entry<JfxInstanceUI, PropertyInformation> entry : uiToPropertyInformation.entrySet()) {
            final JfxInstanceUI ui = entry.getKey();
            ui.targetProperty().unbind();
        }
    }

    private void bind(T target) {

        for (Map.Entry<JfxInstanceUI, PropertyInformation> entry : uiToPropertyInformation.entrySet()) {
            final JfxInstanceUI ui = entry.getKey();
            final PropertyInformation propertyInformation = entry.getValue();

            final Object value;
            try {
                value = propertyInformation.getReadMethod().invoke(target);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOGGER.error("failed to get value for property " + propertyInformation.getDisplayName(), e);
                continue;
            }
            if (value == null) {
                //ui.targetProperty().unbind();
                return;
            }
            final Class<?> valueClass = value.getClass();

            // if the property is a JavaFX-style property, bind directly to it
            if (Property.class.isAssignableFrom(valueClass)) {
                Property<?> jfxProperty = (Property) value;
                ui.targetProperty().bindBidirectional(jfxProperty);
                LOGGER.debug(propertyInformation.getDisplayName() + " bound using javafx.beans.property.Property method");
                continue;
            }

            // otherwise try to bind bidirectionally to a generated JavaBeanObjectProperty
            try {
                Property<?> jfxProperty = JavaBeanObjectPropertyBuilder.create()
                        .bean(target)
                        .name(propertyInformation.getName())
                        .build();
                ui.targetProperty().bindBidirectional(jfxProperty);
                LOGGER.debug(propertyInformation.getDisplayName() + " bound using JavaBeanObjectPropertyBuilder method");
                continue;
            } catch (NoSuchMethodException e) {
                // This happens when we try to use JavaBeanObjectPropertyBuilder on a read-only property
                //LOGGER.debug("DEBUG: " + e.toString());
            }

            // otherwise if the property implements java.util.Observable, register a listener
            if (java.util.Observable.class.isAssignableFrom(valueClass)) {
                java.util.Observable observableValue = (java.util.Observable) value;
                final Observer observer = (observable, o) -> ui.targetProperty().setValue(observable);
                observableValue.addObserver(observer);
                observer.update(observableValue, this);
                LOGGER.debug(propertyInformation.getDisplayName() + " bound using java.util.Observable method");
                continue;
            }

            // otherwise if the property implements javafx.beans.Observable, register a listener
            if (Observable.class.isAssignableFrom(valueClass)) {
                Observable observableValue = (Observable) value;
                final InvalidationListener listener = observable -> ui.targetProperty().setValue(observable);
                observableValue.addListener(listener);
                //FIXME listener is not called when list is changed subsequently; likely because change events are not invalidation events
                listener.invalidated(observableValue);
                LOGGER.debug(propertyInformation.getDisplayName() + " bound using javafx.beans.Observable method");
                continue;
            }

            // otherwise just set the value and put the UI in read-only mode
            LOGGER.warn("no binding for property '" + propertyInformation.getDisplayName() + "'");

            ui.setReadOnly(true);
            ui.targetProperty().setValue(value);
        }

    }


    @Override
    public void setReadOnly(boolean readOnly) {
        //TODO
    }

    protected void propertyUpdated(PropertyInformation propertyInformation, Object value) {
        Node node = nodeToPropertyInformation.inverse().get(propertyInformation);
        if (node instanceof TextField) {
            Bindings.fieldSetValue(getConfiguration(), ((TextField) node), propertyInformation, value);
        }
    }

    protected T getTarget() {
        return targetProperty.get();
    }

    @Override
    public ObjectProperty<T> targetProperty() {
        return targetProperty;
    }
}
