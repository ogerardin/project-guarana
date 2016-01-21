/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.ogerardin.guarana.core.introspection.Introspector;
import com.ogerardin.guarana.core.ui.CollectionUI;
import com.ogerardin.guarana.core.ui.InstanceUI;
import com.ogerardin.guarana.javafx.JfxUiBuilder;
import com.ogerardin.guarana.javafx.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * @author Olivier
 * @since 29/05/15
 */
public class JfxInstanceUI<T> extends JfxUI implements InstanceUI<Parent, T> {

    protected final BeanInfo beanInfo;

    protected BiMap<Control, PropertyDescriptor> controlPropertyDescriptorMap = HashBiMap.create();

    private final VBox root;

    private T target;

    public JfxInstanceUI(JfxUiBuilder builder, Class<T> clazz) {
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
        configureDragSource(title, () -> target);

        // build methods context menu
        configureContextMenu(title, beanInfo, () -> target);

        // build properties form
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
        for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
            String propertyName = propertyDescriptor.getName();
            // ignore hidden properties
            if (getConfiguration().forClass(clazz).isHiddenProperty(propertyName)) {
                continue;
            }

            final Class<?> propertyType = propertyDescriptor.getPropertyType();
            final Method readMethod = propertyDescriptor.getReadMethod();
            final String humanizedName = Introspector.humanize(propertyDescriptor.getDisplayName());
            Label label = new Label(humanizedName);
            label.setTooltip(new Tooltip(propertyDescriptor.toString()));
            grid.add(label, 0, row);

            TextField field = new TextField();
            //FIXME: for now only editable String properties generate an editable text field
            if (Introspector.isReadOnly(propertyDescriptor) || propertyType != String.class) {
                field.setEditable(false);
                field.getStyleClass().add("copyable-label");
            }
//            if (row == 0) {
//                field.requestFocus();
//            }
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
                            writeMethod.invoke(target, value);
                            propertyUpdated(propertyDescriptor, value);
                        } catch (Exception e) {
                            getBuilder().displayException(e);
                        }
                    });

            controlPropertyDescriptorMap.put(field, propertyDescriptor);

            row++;
        }
    }

    private void zoomCollection(Node parent, Method readMethod, String title) {
        try {
            final Collection collection = (Collection) readMethod.invoke(target);
            Class<?> itemClass = Object.class;
            //FIXME how do we get the item class if collection is empty ??
            if (!collection.isEmpty()) {
                itemClass = collection.iterator().next().getClass();
            }
            CollectionUI<Parent, ?> collectionUI = getCollectionUI(itemClass);
            collectionUI.setTarget(collection);
            getBuilder().display(collectionUI, parent, title);

        } catch (Exception e) {
            getBuilder().displayException(e);
        }
    }

    private <I> CollectionUI<Parent, I> getCollectionUI(Class<I> itemClass) {
        return getBuilder().buildCollectionUi(itemClass);
    }

    private <P> void zoomProperty(Node parent, Class<P> propertyType, Method readMethod, String title) {
        try {
            final P value = (P) readMethod.invoke(target);
            getBuilder().displayInstance(value, propertyType, parent, title);
        } catch (Exception ex) {
            getBuilder().displayException(ex);
        }
    }


    public void setTarget(T target) {
        if (this.target != null) {
            unbind();
        }
        this.target = target;
        bind(target);
    }

    private void unbind() {
        for (Map.Entry<Control, PropertyDescriptor> entry : controlPropertyDescriptorMap.entrySet()) {
            Control control = entry.getKey();
            if (control instanceof TextField) {
                ((TextField) control).textProperty().unbind();
            }
        }
    }

    private void bind(T target) {
        JavaBeanObjectPropertyBuilder jfxPropertyBuilder = JavaBeanObjectPropertyBuilder.create();
        jfxPropertyBuilder.bean(target);

        for (Map.Entry<Control, PropertyDescriptor> entry : controlPropertyDescriptorMap.entrySet()) {
            Control control = entry.getKey();
            PropertyDescriptor propertyDescriptor = entry.getValue();

            // TODO handle other field types
            if (control instanceof TextField) {
                jfxPropertyBuilder.name(propertyDescriptor.getName());
                Property jfxProperty = null;
                try {
                    jfxProperty = jfxPropertyBuilder.build();
                } catch (NoSuchMethodException e) {
                    System.err.println("WARNING: " + e.toString());
                }

                if (jfxProperty == null) {
                    // failed to create JavaFX Property: just set the field value (no binding)
                    try {
                        Object value = propertyDescriptor.getReadMethod().invoke(target);
                        Bindings.fieldSetValue(getConfiguration(), (TextField) control, propertyDescriptor, value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                if (propertyDescriptor.getPropertyType() == String.class) {
                    ((TextField) control).textProperty().bindBidirectional(jfxProperty);
                } else {
                    //TODO handle other property types
                    System.err.println("ERROR: no binding for type " + propertyDescriptor.getPropertyType());
                }
            }
        }
    }


    @Override
    public Parent render() {
        return root;
    }

    protected void propertyUpdated(PropertyDescriptor propertyDescriptor, Object value) {
        Control control = controlPropertyDescriptorMap.inverse().get(propertyDescriptor);
        if (control instanceof TextField) {
            Bindings.fieldSetValue(getConfiguration(), ((TextField) control), propertyDescriptor, value);
        }
    }

    protected T getTarget() {
        return target;
    }
}
