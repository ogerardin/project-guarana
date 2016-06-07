/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.ogerardin.guarana.core.introspection.Introspector;
import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.binding.Bindings;
import com.ogerardin.guarana.javafx.ui.JfxCollectionUI;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
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
public class DefaultJfxInstanceUI<T> extends JfxUI implements JfxInstanceUI<T> {

    protected final BeanInfo beanInfo;

    protected BiMap<Node, PropertyDescriptor> nodePropertyDescriptorMap = HashBiMap.create();
    protected BiMap<JfxInstanceUI, PropertyDescriptor> uiPropertyDescriptorMap = HashBiMap.create();

    private final VBox root;

    private ObjectProperty<T> targetProperty = new SimpleObjectProperty<>();

    public DefaultJfxInstanceUI(JfxUiManager builder, Class<T> clazz) {
        super(builder);

        beanInfo = Introspector.getClassInfo(clazz);

        root = new VBox();
        final BeanDescriptor beanDescriptor = beanInfo.getBeanDescriptor();
        final String className = beanDescriptor.getBeanClass().getSimpleName();
        String displayName = beanDescriptor.getDisplayName();
        if (displayName.equals(className) && getConfiguration().getHumanizeClassNames()) {
            displayName = Introspector.humanize(className);
        }
        final Label title = new Label(displayName);
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        root.getChildren().add(title);

        // set the title label as a source for drag and drop
        title.setGraphic(new ImageView(ICON_DRAG_HANDLE));
        configureDragSource(title, this::getTarget);

        // build methods context menu
        configureContextMenu(title, beanInfo, this::getTarget);

        // build properties form
        GridPane grid = buildGridPane();
        root.getChildren().add(grid);
        int row = 0;
        for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
            String propertyName = propertyDescriptor.getName();
            // ignore hidden properties
            if (getConfiguration().isHiddenProperty(clazz, propertyName)) {
                continue;
            }

            final Class<?> propertyType = propertyDescriptor.getPropertyType();
            final Method readMethod = propertyDescriptor.getReadMethod();
            final String humanizedName = Introspector.humanize(propertyDescriptor.getDisplayName());
            Label label = new Label(humanizedName);
            label.setTooltip(new Tooltip(propertyDescriptor.toString()));
            grid.add(label, 0, row);

            JfxInstanceUI<?> fieldUi = getBuilder().buildEmbeddedInstanceUI(propertyType);
            Node field = fieldUi.render();
            grid.add(field, 1, row);

            // if it's a collection, add a button to open as list
            if (Collection.class.isAssignableFrom(propertyType)) {
                Button button = new Button("...");
                button.setOnAction(e -> zoomCollection(button, readMethod, humanizedName));
                grid.add(button, 2, row);
            }
            // otherwise add a button to zoom on property
            else {
                Button button = new Button("...");
                button.setOnAction(e -> zoomProperty(button, propertyType, readMethod, humanizedName));
                grid.add(button, 2, row);
            }

            // set the field as a target for drag and drop
            configureDropTarget(field,
                    value -> {
                        Class<?> targetPropertyClass = propertyDescriptor.getPropertyType();
                        return targetPropertyClass.isAssignableFrom(value.getClass());
                    },
                    value -> {
                        Method writeMethod = propertyDescriptor.getWriteMethod();
                        try {
                            writeMethod.invoke(getTarget(), value);
                            //FIXME if the UI's target property is bound to a JavaBeanObjectProperty, we should call fireValueChangedEvent
                            propertyUpdated(propertyDescriptor, value);
                        } catch (Exception e) {
                            getBuilder().displayException(e);
                        }
                    });

            uiPropertyDescriptorMap.put(fieldUi, propertyDescriptor);
            nodePropertyDescriptorMap.put(field, propertyDescriptor);

            row++;
        }
    }

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

    private void zoomCollection(Node parent, Method readMethod, String title) {
        try {
            final Collection collection = (Collection) readMethod.invoke(getTarget());
            Class<?> itemClass = Object.class;
            //FIXME how do we get the item class if collection is empty ??
            if (!collection.isEmpty()) {
                itemClass = collection.iterator().next().getClass();
            }
            JfxCollectionUI<?> collectionUI = getCollectionUI(itemClass);
            collectionUI.setTarget(collection);
            getBuilder().display(collectionUI, parent, title);

        } catch (Exception e) {
            getBuilder().displayException(e);
        }
    }

    private <I> JfxCollectionUI<I> getCollectionUI(Class<I> itemClass) {
        return getBuilder().buildCollectionUi(itemClass);
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
        for (Map.Entry<JfxInstanceUI, PropertyDescriptor> entry : uiPropertyDescriptorMap.entrySet()) {
            final JfxInstanceUI ui = entry.getKey();
            ui.targetProperty().unbind();
        }
    }

    private void bind(T target) {

        for (Map.Entry<JfxInstanceUI, PropertyDescriptor> entry : uiPropertyDescriptorMap.entrySet()) {
            final JfxInstanceUI ui = entry.getKey();
            final PropertyDescriptor propertyDescriptor = entry.getValue();
            //final Class<?> propertyType = propertyDescriptor.getPropertyType();

            final Object value;
            try {
                value = propertyDescriptor.getReadMethod().invoke(target);
            } catch (IllegalAccessException | InvocationTargetException e) {
                System.err.println("ERROR: failed to get value for property " + propertyDescriptor.getDisplayName());
                e.printStackTrace();
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
                System.out.println("DEBUG: " + propertyDescriptor.getDisplayName() + " bound using javafx.beans.property.Property method");
                continue;
            }

            // otherwise try to bind bidirectionally to a generated JavaBeanObjectProperty
            try {
                Property<?> jfxProperty = JavaBeanObjectPropertyBuilder.create()
                        .bean(target)
                        .name(propertyDescriptor.getName())
                        .build();
                ui.targetProperty().bindBidirectional(jfxProperty);
                System.out.println("DEBUG: " + propertyDescriptor.getDisplayName() + " bound using JavaBeanObjectPropertyBuilder method");
                continue;
            } catch (NoSuchMethodException e) {
                // This happens when we try to use JavaBeanObjectPropertyBuilder on a read-only property
                //System.out.println("DEBUG: " + e.toString());
            }

            // otherwise if the property implements java.util.Observable, register a listener
            if (java.util.Observable.class.isAssignableFrom(valueClass)) {
                java.util.Observable observableValue = (java.util.Observable) value;
                final Observer observer = (observable, o) -> ui.targetProperty().setValue(observable);
                observableValue.addObserver(observer);
                observer.update(observableValue, this);
                System.out.println("DEBUG: " + propertyDescriptor.getDisplayName() + " bound using java.util.Observable method");
                continue;
            }

            // otherwise if the property implements javafx.beans.Observable, register a listener
            if (Observable.class.isAssignableFrom(valueClass)) {
                Observable observableValue = (Observable) value;
                final InvalidationListener listener = observable -> ui.targetProperty().setValue(observable);
                observableValue.addListener(listener);
                //FIXME listener is not called when list is changed subsequently; likely because change events are not invalidation events
                listener.invalidated(observableValue);
                System.out.println("DEBUG: " + propertyDescriptor.getDisplayName() + " bound using javafx.beans.Observable method");
                continue;
            }

            // otherwise just set the value and put the UI in read-only mode
            System.err.println("WARNING: no binding for property '" + propertyDescriptor.getDisplayName() + "'");

            ui.setReadOnly(true);
            ui.targetProperty().setValue(value);
        }

    }


    @Override
    public Parent render() {
        return root;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        //TODO
    }

    protected void propertyUpdated(PropertyDescriptor propertyDescriptor, Object value) {
        Node node = nodePropertyDescriptorMap.inverse().get(propertyDescriptor);
        if (node instanceof TextField) {
            Bindings.fieldSetValue(getConfiguration(), ((TextField) node), propertyDescriptor, value);
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
