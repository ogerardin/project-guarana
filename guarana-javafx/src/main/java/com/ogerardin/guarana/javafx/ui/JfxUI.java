/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui;

import com.ogerardin.guarana.core.config.Configuration;
import com.ogerardin.guarana.core.introspection.Introspector;
import com.ogerardin.guarana.core.registry.Identifier;
import com.ogerardin.guarana.core.registry.ObjectRegistry;
import com.ogerardin.guarana.core.ui.Renderable;
import com.ogerardin.guarana.core.ui.Wrapper;
import com.ogerardin.guarana.javafx.JfxUiBuilder;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.apache.commons.lang.Validate;

import java.beans.BeanInfo;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Common abstract superclass for Java FX renderable classes.
 * @author oge
 * @since 24/09/2015
 */
public abstract class JfxUI implements Renderable<Parent> {

    private final JfxUiBuilder builder;

    public JfxUI(JfxUiBuilder builder) {
        Validate.notNull(builder);
        this.builder = builder;
    }

    <T> void configureContextMenu(Control control, BeanInfo beanInfo, Wrapper<T> wrapper) {
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
        control.setContextMenu(contextMenu);
    }

    static <T> void configureDragSource(Node source, Wrapper<T> wrapper) {
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

    /**
     * Configures the specified control so that it becomes a target for drag-and-drop operations. The control will
     * accept custom content type "application/x.object-reference" for linking; the content value is expected to be
     * an {@link Identifier} which will be resolved to actual object reference through {@link ObjectRegistry} and
     * used to set the specified property.
     *
     * @param control            The visual element that will be enabled as a drag-and-drop target
     * @param propertyDescriptor the property the will be affected by the drop operation
     * @param wrapper            wrapper used to retrieve the target instance
     * @param <T>                The target type as returned by the wrapper
     */
    <T> void configureDropTarget(Control control, PropertyDescriptor propertyDescriptor, Wrapper<T> wrapper) {
        control.setOnDragOver(event -> {
            // for some reason you can't accept the transfer in the DragEntered handler, you have to do it
            // in the DragOver handler
            Dragboard db = event.getDragboard();
            T target = wrapper.getInstance();
            if (db.hasContent(Const.DATA_FORMAT_OBJECT_IDENTIFIER)
                    && handleDragDroppedUsingIdentifier(propertyDescriptor, db, target, true)) {
                event.acceptTransferModes(TransferMode.LINK);
            }
            event.consume();
        });
        control.setOnDragDropped(event -> {
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

    private <T> boolean handleDragDroppedUsingIdentifier(PropertyDescriptor propertyDescriptor, Dragboard db,
                                                         T target, boolean validateOnly) {
        // retrieve identifier from dragboard and associated source object in registry
        Identifier identifier = (Identifier) db.getContent(Const.DATA_FORMAT_OBJECT_IDENTIFIER);
        Object source = ObjectRegistry.INSTANCE.get(identifier);
        if (source == null) {
            return false;
        }

        // if only validating, assert type compatibility between source object and target property
        Class targetPropertyClass = propertyDescriptor.getPropertyType();
        if (validateOnly) {
            return targetPropertyClass.isAssignableFrom(source.getClass());
        }

        // invoke property setter on target instance
        try {
            final Method writeMethod = propertyDescriptor.getWriteMethod();
            writeMethod.invoke(target, source);
            //field.setText(source.toString());
        } catch (Exception e) {
            getBuilder().displayException(e);
            return false;
        }
        return true;
    }

    public JfxUiBuilder getBuilder() {
        return builder;
    }

    protected Configuration getConfiguration() {
        return getBuilder().getConfiguration();
    }

    /**
     * A specialized MenuItem that triggers a method call
     *
     * @param <T> type of the target object
     */
    private class MethodMenuItem<T> extends MenuItem {
        public MethodMenuItem(MethodDescriptor md, Wrapper<T> wrapper) {
            super(md.getMethod().toGenericString());
            setOnAction(event -> executeMethodRequested(md, wrapper));
        }
    }

    /**
     * A specialized menuItem that triggers a constructor call
     * @param <T> the target class
     */
    private class ConstructorMenuItem<T> extends MenuItem {
        public ConstructorMenuItem(Constructor<T> constructor) {
            super(constructor.toGenericString());
            setOnAction(event -> executeConstructorRequested(constructor));
        }
    }

    private <T> void executeConstructorRequested(Constructor<T> constructor) {
        System.out.println(constructor.toGenericString());
        T instance;
        if (constructor.getParameterCount() == 0) {
            try {
                instance = constructor.newInstance();
                getBuilder().displayInstance(instance, constructor.getDeclaringClass(), "New Instance");
            } catch (Exception e) {
                e.printStackTrace();
                getBuilder().displayException(e);
            }
        } else {
            JfxMethodCallUI methodCallUI = new JfxMethodCallUI(getBuilder(), constructor);
            getBuilder().display(methodCallUI);
        }
    }

    private <T, R> void executeMethodRequested(MethodDescriptor md, Wrapper<T> wrapper) {
        System.out.println(md.getName());
        Method method = md.getMethod();

        final T target = wrapper.getInstance();
        final Class<R> returnType = (Class<R>) method.getReturnType();
        // if no arg, execute immediately, otherwise display arg dialog
        if (method.getParameterCount() == 0) {
            try {
                R result = (R) method.invoke(target);
                getBuilder().displayInstance(result, returnType, "Result");
            } catch (Exception e) {
                e.printStackTrace();
                getBuilder().displayException(e);
            }
        } else {
            JfxMethodCallUI methodCallUI = new JfxMethodCallUI(getBuilder(), method);
            getBuilder().display(methodCallUI);
        }
    }

}
