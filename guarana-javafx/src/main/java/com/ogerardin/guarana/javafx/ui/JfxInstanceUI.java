/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui;

import com.ogerardin.guarana.core.config.ConfigManager;
import com.ogerardin.guarana.core.introspection.Introspector;
import com.ogerardin.guarana.core.registry.Identifier;
import com.ogerardin.guarana.core.registry.ObjectRegistry;
import com.ogerardin.guarana.core.ui.CollectionUI;
import com.ogerardin.guarana.core.ui.InstanceUI;
import com.ogerardin.guarana.javafx.JfxUiBuilder;
import com.ogerardin.guarana.javafx.util.DialogUtil;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import jfxtras.labs.scene.control.BeanPathAdapter;

import java.beans.BeanInfo;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Olivier on 29/05/15.
 */
public class JfxInstanceUI<T> implements InstanceUI<Parent, T> {

    protected final BeanInfo beanInfo;

    protected Map<PropertyDescriptor, Control> propertyDescriptorControlMap = new HashMap<PropertyDescriptor, Control>();
    protected Map<Control, PropertyDescriptor> controlPropertyDescriptorMap = new HashMap<Control, PropertyDescriptor>();

    private final VBox root;

    private T target;

    public JfxInstanceUI(Class<T> clazz) {
        beanInfo = Introspector.getClassInfo(clazz);

        root = new VBox();
        final String className = beanInfo.getBeanDescriptor().getBeanClass().getSimpleName();
        String displayName = beanInfo.getBeanDescriptor().getDisplayName();
        if (displayName.equals(className) && ConfigManager.getHumanizeClassNames()) {
            displayName = Introspector.humanize(className);
        }
        final Label title = new Label(displayName);
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        root.getChildren().add(title);

        // set the title label as a source for drag and drop
        configureDragDropSource(title);

        // build methods context menu
        {
            ContextMenu contextMenu = getContextMenu(beanInfo);
            title.setContextMenu(contextMenu);
            //root.setOnMouseClicked(event -> contextMenu.show(root, Side.BOTTOM, 0, 0));
        }

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
            // ignore "class" property
            if (propertyDescriptor.getName().equals("class")) {
                continue;
            }

            final Class<?> propertyType = propertyDescriptor.getPropertyType();
            final Method readMethod = propertyDescriptor.getReadMethod();
            final String humanizedName = Introspector.humanize(propertyDescriptor.getDisplayName());
            Label label = new Label(humanizedName);
            label.setTooltip(new Tooltip(propertyDescriptor.toString()));
            grid.add(label, 0, row);

            TextField field = new TextField();
            grid.add(field, 1, row);
            //FIXME: for now only editable String properties generate an editable text field
            if (Introspector.isReadOnly(propertyDescriptor) || propertyType != String.class) {
                field.setEditable(false);
            }
            if (row == 0) {
                field.requestFocus();
            }

            // if it's a collection, add a button to open as list
            if (Collection.class.isAssignableFrom(propertyType)) {
                Button button = new Button("...");
                button.setOnAction(e -> {
                    zoomCollection(readMethod, humanizedName);
                });
                grid.add(button, 2, row);
            }
            // otherwise add a button to zoom on property
            else {
                Button button = new Button("...");
                button.setOnAction(e -> {
                    zoomProperty(propertyType, readMethod, humanizedName);
                });
                grid.add(button, 2, row);
            }

            // set the field as a target for drag and drop
            configureDragDropTarget(field);

            propertyDescriptorControlMap.put(propertyDescriptor, field);
            controlPropertyDescriptorMap.put(field, propertyDescriptor);

            row++;
        }
    }

    private void configureDragDropSource(Node source) {
        source.setOnDragDetected(event -> {
            Dragboard dragboard = source.startDragAndDrop(TransferMode.LINK);
            ClipboardContent content = new ClipboardContent();
            Identifier identifier = ObjectRegistry.INSTANCE.put(target);
            content.put(Const.DATA_FORMAT_OBJECT_IDENTIFIER, identifier);
            dragboard.setContent(content);
            event.consume();
        });
        source.setOnDragDone(event -> {
            if (event.getTransferMode() == TransferMode.LINK) {
                // drag and drop done successfully, nothing to do here
            }
            event.consume();
        });
    }

    private static void configureDragDropTarget(TextField field) {
        field.setOnDragOver(event -> {
            event.acceptTransferModes(TransferMode.LINK);
            event.consume();
        });
        field.setOnDragEntered(event -> {
            field.setCursor(Cursor.CROSSHAIR);
            event.consume();
        });
        field.setOnDragExited(event -> {
            field.setCursor(Cursor.DEFAULT);
            event.consume();
        });
        field.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasContent(Const.DATA_FORMAT_OBJECT_IDENTIFIER)) {
                Identifier identifier = (Identifier) db.getContent(Const.DATA_FORMAT_OBJECT_IDENTIFIER);
                System.out.println(identifier);
                Object source = ObjectRegistry.INSTANCE.get(identifier);
                // FIXME set actual value
                field.setText(source.toString());
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void zoomCollection(Method readMethod, String title) {
        try {
            final Collection collection = (Collection) readMethod.invoke(target);
            Class<?> itemClass = Object.class;
            //FIXME how do we get the item class if collection is empty ??
            if (!collection.isEmpty()) {
                itemClass = collection.iterator().next().getClass();
            }
            CollectionUI<Parent, ?> collectionUI = getCollectionUI(itemClass);
            collectionUI.setTarget(collection);
            DialogUtil.display(collectionUI, title);

        } catch (Exception ex) {
            DialogUtil.displayException(ex);
        }
    }

    private static <I> CollectionUI<Parent, I> getCollectionUI(Class<I> itemClass) {
        return JfxUiBuilder.INSTANCE.buildCollectionUi(itemClass);
    }

    private <P> void zoomProperty(Class<P> propertyType, Method readMethod, String title) {
        try {
            final P value = (P) readMethod.invoke(target);
            DialogUtil.displayInstance(propertyType, value, title);
        } catch (Exception ex) {
            DialogUtil.displayException(ex);
        }
    }

    private ContextMenu getContextMenu(BeanInfo beanInfo) {
        ContextMenu contextMenu = new ContextMenu();
        // add methods
        Arrays.asList(beanInfo.getMethodDescriptors()).stream()
                .filter(md -> !Introspector.isGetterOrSetter(md))
                .map(md -> new MenuItem(md.getMethod().toGenericString()) {
                    {
                        setOnAction(event -> executeMethodRequested(md));
                    }
                })
                .forEach(menuItem -> contextMenu.getItems().add(menuItem));
        // add constructors
        Arrays.asList(beanInfo.getBeanDescriptor().getBeanClass().getDeclaredConstructors()).stream()
                .map(constructor -> new MenuItem(constructor.toGenericString()) {
                    {
                        setOnAction(event -> executeConstructorRequested(constructor));
                    }
                })
                .forEach(menuItem -> contextMenu.getItems().add(menuItem));
        return contextMenu;
    }

    private <T> void executeConstructorRequested(Constructor<T> constructor) {
        System.out.println(constructor.toGenericString());

        T instance;
        if (constructor.getParameterCount() ==0) {
            try {
                instance = constructor.newInstance();
                DialogUtil.displayInstance((Class<T>) beanInfo.getClass(), instance, "New Instance");
            } catch (Exception e) {
                e.printStackTrace();
                DialogUtil.displayException(e);
            }
        }
        else {
            JfxMethodCallUI methodCallUI = new JfxMethodCallUI(constructor);
            DialogUtil.display(methodCallUI);
        }
    }

    private void executeMethodRequested(MethodDescriptor md) {
        System.out.println(md.getName());
        Method method = md.getMethod();

        final Class returnType = (Class) method.getReturnType();

        Object result;
        // if no arg, execute immediately, otherwise display arg dialog
        if (method.getParameterCount() == 0) {
            try {
                result = method.invoke(target);
                DialogUtil.displayInstance(returnType, result, "Result");
            } catch (Exception e) {
                e.printStackTrace();
                DialogUtil.displayException(e);
            }
        } else {
            JfxMethodCallUI methodCallUI = new JfxMethodCallUI(method);
            DialogUtil.display(methodCallUI);
        }
    }


    public void setTarget(T target) {
        if (this.target != null) {
            unbind(this.target);
        }
        this.target = target;
        bind(target);
    }

    private void unbind(T target) {
        //TODO
    }

    private void bind(T target) {
        for (Map.Entry<Control, PropertyDescriptor> entry : controlPropertyDescriptorMap.entrySet()) {
            Control control = entry.getKey();
            PropertyDescriptor propertyDescriptor = entry.getValue();

            if (control instanceof TextField) {
                TextField textField = (TextField) control;

                if (textField.isEditable()) {
                    BeanPathAdapter<T> beanPathAdapter = new BeanPathAdapter<T>(target);
                    String propertyName = propertyDescriptor.getName();
                    beanPathAdapter.bindBidirectional(propertyName, textField.textProperty());
                } else {
                    //FIXME we should bind (unidirectionally) and not just set property value
                    try {
                        textField.setText(propertyDescriptor.getReadMethod().invoke(target).toString());
                    } catch (Exception ignored) {
                    }
                }


            }

        }
    }

    @Override
    public Parent render() {
        return root;
    }
}
