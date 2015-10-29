/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui;

import com.ogerardin.guarana.core.introspection.Introspector;
import com.ogerardin.guarana.core.registry.Identifier;
import com.ogerardin.guarana.core.registry.ObjectRegistry;
import com.ogerardin.guarana.core.ui.Wrapper;
import com.ogerardin.guarana.javafx.util.DialogUtil;
import javafx.event.Event;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.beans.BeanInfo;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

/*
 * Created by oge on 24/09/2015.
 */
public abstract class JfxUI {

    static <T> ContextMenu getContextMenu(BeanInfo beanInfo, Wrapper<T> wrapper) {
        ContextMenu contextMenu = new ContextMenu();
        // add methods
        Arrays.asList(beanInfo.getMethodDescriptors()).stream()
                .filter(md -> !Introspector.isGetterOrSetter(md))
                .map(md -> new MethodMenuItem<>(md, wrapper))
                .forEach(menuItem -> contextMenu.getItems().add(menuItem));
        // add constructors
        Arrays.asList(beanInfo.getBeanDescriptor().getBeanClass().getDeclaredConstructors()).stream()
                .map(constructor -> new ConstructorMenuItem(constructor))
                .forEach(menuItem -> contextMenu.getItems().add(menuItem));
        return contextMenu;
    }

    static <T> void configureDragDropSource(Node source, Wrapper<T> wrapper) {
        source.setOnDragDetected(event -> {
            Dragboard dragboard = source.startDragAndDrop(TransferMode.LINK);
            ClipboardContent content = new ClipboardContent();
            Identifier identifier = ObjectRegistry.INSTANCE.put(wrapper.getInstance());
            content.put(Const.DATA_FORMAT_OBJECT_IDENTIFIER, identifier);
            dragboard.setContent(content);
            event.consume();
        });
        // drag done: nothing to do, just consume the event
        source.setOnDragDone(Event::consume);
    }

    static <T> void configureDragDropTarget(TextField field, PropertyDescriptor propertyDescriptor, Wrapper<T> wrapper) {
        field.setOnDragOver(event -> {
            event.acceptTransferModes(TransferMode.LINK);
            event.consume();
        });
        field.setOnDragEntered(event -> {
            Dragboard db = event.getDragboard();
            T target = wrapper.getInstance();
            if (db.hasContent(Const.DATA_FORMAT_OBJECT_IDENTIFIER)
                    && handleDragDroppedUsingIdentifier(propertyDescriptor, db, target, true)) {
                System.out.println("ACCEPT");
//                event.acceptTransferModes(TransferMode.LINK);
//                field.setCursor(Cursor.CROSSHAIR);
            }
            event.consume();
        });
        field.setOnDragExited(event -> {
            field.setCursor(Cursor.DEFAULT);
            event.consume();
        });
        field.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            T target = wrapper.getInstance();
            boolean success = false;
            if (db.hasContent(Const.DATA_FORMAT_OBJECT_IDENTIFIER)) {
                success = handleDragDroppedUsingIdentifier(propertyDescriptor, db, target, false);
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private static <T> boolean handleDragDroppedUsingIdentifier(PropertyDescriptor propertyDescriptor, Dragboard db,
                                                                T target, boolean validateOnly) {
        // retrieve identifier from dragboard and associated source object in registry
        Identifier identifier = (Identifier) db.getContent(Const.DATA_FORMAT_OBJECT_IDENTIFIER);
        Object source = ObjectRegistry.INSTANCE.get(identifier);
        System.out.println(identifier + " -> " + source.toString());

        // if only validating, assert type compatibility between source object and target property
        Class targetPropertyClass = propertyDescriptor.getPropertyType();
        if (validateOnly) {
            System.out.println(targetPropertyClass);
            System.out.println(source.getClass());
            return targetPropertyClass.isAssignableFrom(source.getClass());
        }

        // invoke property setter on target instance
        try {
            final Method writeMethod = propertyDescriptor.getWriteMethod();
            writeMethod.invoke(target, source);
            //field.setText(source.toString());
        } catch (Exception e) {
            DialogUtil.displayException(e);
            return false;
        }
        return true;
    }

    /**
     * A specialized MenuItem that triggers a method call
     *
     * @param <T> type of the target object
     */
    private static class MethodMenuItem<T> extends MenuItem {
        public MethodMenuItem(MethodDescriptor md, Wrapper<T> wrapper) {
            super(md.getMethod().toGenericString());
            setOnAction(event -> executeMethodRequested(md, wrapper));
        }
    }

    /**
     * A specialized menuItem that triggers a constructor call
     * @param <T> the target class
     */
    private static class ConstructorMenuItem<T> extends MenuItem {
        public ConstructorMenuItem(Constructor<T> constructor) {
            super(constructor.toGenericString());
            setOnAction(event -> executeConstructorRequested(constructor));
        }
    }

    private static <T> void executeConstructorRequested(Constructor<T> constructor) {
        System.out.println(constructor.toGenericString());
        T instance;
        if (constructor.getParameterCount() == 0) {
            try {
                instance = constructor.newInstance();
                DialogUtil.displayInstance(constructor.getDeclaringClass(), instance, "New Instance");
            } catch (Exception e) {
                e.printStackTrace();
                DialogUtil.displayException(e);
            }
        } else {
            JfxMethodCallUI methodCallUI = new JfxMethodCallUI(constructor);
            DialogUtil.display(methodCallUI);
        }
    }

    private static <T, R> void executeMethodRequested(MethodDescriptor md, Wrapper<T> wrapper) {
        System.out.println(md.getName());
        Method method = md.getMethod();

        final T target = wrapper.getInstance();
        final Class<R> returnType = (Class<R>) method.getReturnType();
        // if no arg, execute immediately, otherwise display arg dialog
        if (method.getParameterCount() == 0) {
            try {
                R result = (R) method.invoke(target);
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

}
