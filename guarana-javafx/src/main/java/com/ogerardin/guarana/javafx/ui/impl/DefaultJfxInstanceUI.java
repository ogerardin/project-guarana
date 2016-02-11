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
import com.ogerardin.guarana.javafx.ui.JfxRenderable;
import javafx.beans.property.Property;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
public class DefaultJfxInstanceUI<T> extends JfxUI implements JfxInstanceUI<T> {

    protected final BeanInfo beanInfo;

    protected BiMap<Node, PropertyDescriptor> controlPropertyDescriptorMap = HashBiMap.create();

    private final VBox root;

    private T target;

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
        configureDragSource(title, () -> target);

        // build methods context menu
        configureContextMenu(title, beanInfo, () -> target);

        // build properties form
        GridPane grid = buildGridPane();

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

            JfxRenderable fieldUi = buildEmbeddedUi(propertyDescriptor, propertyType);
            Parent field = fieldUi.render();
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

    private JfxRenderable buildEmbeddedUi(PropertyDescriptor propertyDescriptor, Class<?> propertyType) {
        Class uiClass = getConfiguration().forClass(propertyType).getEmbeddedUiClass();
        if (uiClass != null) {
            try {
                // might throw ClassCastException if the specified class doesn't match JfxRenderable
                return (JfxRenderable) uiClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return new DefaultJfxEmbeddedInstanceUI<>(getBuilder(), propertyType);

//        TextField field = new TextField();
//        //FIXME: for now only editable String properties generate an editable text field
//        if (Introspector.isReadOnly(propertyDescriptor) || propertyType != String.class) {
//            field.setEditable(false);
//            field.getStyleClass().add("copyable-label");
//        }
//        return field;
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
            final Collection collection = (Collection) readMethod.invoke(target);
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
        for (Map.Entry<Node, PropertyDescriptor> entry : controlPropertyDescriptorMap.entrySet()) {
            Node node = entry.getKey();
            if (node instanceof TextField) {
                ((TextField) node).textProperty().unbind();
            }
        }
    }

    private void bind(T target) {

        for (Map.Entry<Node, PropertyDescriptor> entry : controlPropertyDescriptorMap.entrySet()) {
            Node node = entry.getKey();
            PropertyDescriptor propertyDescriptor = entry.getValue();

            // TODO handle other field types
            if (node instanceof TextField) {
                Property jfxProperty = null;
                try {
                    jfxProperty = JavaBeanObjectPropertyBuilder.create()
                            .bean(target)
                            .name(propertyDescriptor.getName())
                            .build();
                } catch (NoSuchMethodException e) {
                    System.err.println("WARNING: " + e.toString());
                }

                if (jfxProperty == null) {
                    // failed to create JavaFX Property: just set the field value (no binding)
                    try {
                        Object value = propertyDescriptor.getReadMethod().invoke(target);
                        Bindings.fieldSetValue(getConfiguration(), (TextField) node, propertyDescriptor, value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                if (propertyDescriptor.getPropertyType() == String.class) {
                    ((TextField) node).textProperty().bindBidirectional(jfxProperty);
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
        Node node = controlPropertyDescriptorMap.inverse().get(propertyDescriptor);
        if (node instanceof TextField) {
            Bindings.fieldSetValue(getConfiguration(), ((TextField) node), propertyDescriptor, value);
        }
    }

    protected T getTarget() {
        return target;
    }
}
