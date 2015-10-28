/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui;

import com.ogerardin.guarana.core.introspection.Introspector;
import com.ogerardin.guarana.core.registry.Identifier;
import com.ogerardin.guarana.core.registry.ObjectRegistry;
import com.ogerardin.guarana.core.ui.Wrapper;
import com.ogerardin.guarana.javafx.util.DialogUtil;
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
        source.setOnDragDone(event -> {
            if (event.getTransferMode() == TransferMode.LINK) {
                // drag and drop done successfully, nothing to do here
            }
            event.consume();
        });
    }

    static void configureDragDropTarget(TextField field) {
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

    private static class MethodMenuItem<T> extends MenuItem {
        public MethodMenuItem(MethodDescriptor md, Wrapper<T> wrapper) {
            super(md.getMethod().toGenericString());
            setOnAction(event -> executeMethodRequested(md, wrapper));
        }
    }

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
