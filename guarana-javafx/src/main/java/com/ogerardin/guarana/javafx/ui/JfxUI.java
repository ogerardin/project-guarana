/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui;

import com.ogerardin.guarana.core.config.Configuration;
import com.ogerardin.guarana.core.introspection.Introspector;
import com.ogerardin.guarana.core.registry.Identifier;
import com.ogerardin.guarana.core.registry.ObjectRegistry;
import com.ogerardin.guarana.core.ui.MapUI;
import com.ogerardin.guarana.core.ui.Renderable;
import com.ogerardin.guarana.javafx.JfxUiBuilder;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.apache.commons.lang.Validate;

import java.beans.BeanInfo;
import java.beans.MethodDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Common abstract superclass for Java FX renderable classes.
 * @author oge
 * @since 24/09/2015
 */
public abstract class JfxUI implements Renderable<Parent> {

    Image ICON_CONSTRUCTOR = new Image("call_class_16.png");
    Image ICON_METHOD = new Image("call_method_16.png");

    private final JfxUiBuilder builder;

    public JfxUI(JfxUiBuilder builder) {
        Validate.notNull(builder);
        this.builder = builder;
    }

    <T> void configureContextMenu(Control control, BeanInfo beanInfo, Supplier<T> targetSupplier) {
        ContextMenu contextMenu = new ContextMenu();
        // add methods
        Arrays.asList(beanInfo.getMethodDescriptors()).stream()
                .filter(md -> !Introspector.isGetterOrSetter(md))
                .map(md -> new MethodMenuItem<>(md, targetSupplier, new ImageView(ICON_METHOD)))
                .forEach(menuItem -> contextMenu.getItems().add(menuItem));

        contextMenu.getItems().add(new SeparatorMenuItem());
        // add constructors
        Arrays.asList(beanInfo.getBeanDescriptor().getBeanClass().getDeclaredConstructors()).stream()
                .map(c -> new ConstructorMenuItem<>(c, new ImageView(ICON_CONSTRUCTOR)))
                .forEach(menuItem -> contextMenu.getItems().add(menuItem));
        control.setContextMenu(contextMenu);

        contextMenu.getItems().add(new SeparatorMenuItem());
        // add other items
        Arrays.asList(Introspector.getClassInfo(this.getClass()).getMethodDescriptors()).stream()
                .filter(md -> md.getName().equals("displayObjectRegistry"))
                .map(md -> new MethodMenuItem<>(md, () -> this, null))
                .forEach(menuItem -> contextMenu.getItems().add(menuItem));
    }

    static <T> void configureDragSource(Node source, Supplier<T> targetSupplier) {
        source.setOnDragDetected(event -> {
            Dragboard dragboard = source.startDragAndDrop(TransferMode.LINK);
            ClipboardContent content = new ClipboardContent();
            Identifier identifier = ObjectRegistry.INSTANCE.put(targetSupplier.get());
            content.put(Const.DATA_FORMAT_OBJECT_IDENTIFIER, identifier);
            dragboard.setContent(content);
            event.consume();
        });
        // drag done: nothing to do, just consume the event
        source.setOnDragDone(Event::consume);
    }

    void configureDropTarget(Control control, ValueValidator valueValidator, ValueSetter valueSetter) {
        control.setOnDragOver(event -> {
            // for some reason you can't accept the transfer in the DragEntered handler, you have to do it
            // in the DragOver handler (which is called whenever the pointer moves inside the target)
            Dragboard db = event.getDragboard();
            if (db.hasContent(Const.DATA_FORMAT_OBJECT_IDENTIFIER)) {
                // retrieve identifier from dragboard and associated source object in registry
                Identifier identifier = (Identifier) db.getContent(Const.DATA_FORMAT_OBJECT_IDENTIFIER);
                Object source = ObjectRegistry.INSTANCE.get(identifier);
                if (source == null) {
                    System.err.println("Identifier not found in object registry: " + identifier);
                    return;
                }
                if (valueValidator.validateValue(source)) {
                    event.acceptTransferModes(TransferMode.LINK);
                }
            }
            event.consume();
        });
        control.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(Const.DATA_FORMAT_OBJECT_IDENTIFIER)) {
                // retrieve identifier from dragboard and associated source object in registry
                Identifier identifier = (Identifier) db.getContent(Const.DATA_FORMAT_OBJECT_IDENTIFIER);
                Object source = ObjectRegistry.INSTANCE.get(identifier);
                if (source == null) {
                    System.err.println("Identifier not found in object registry: " + identifier);
                    return;
                }

                boolean completed = false;
                try {
                    valueSetter.setValue(source);
                    completed = true;
                } catch (Exception e) {
                    getBuilder().displayException(e);
                }
                event.setDropCompleted(completed);
            }
            event.consume();
        });
    }

    public JfxUiBuilder getBuilder() {
        return builder;
    }

    protected Configuration getConfiguration() {
        return getBuilder().getConfiguration();
    }

    /**
     * A specialized MenuItem that triggers a method call
     * @param <T> type of the target object
     */
    private class MethodMenuItem<T> extends MenuItem {
        public MethodMenuItem(MethodDescriptor md, Supplier<T> supplier, ImageView icon) {
            super(md.getMethod().toGenericString());
            setOnAction(event -> executeMethodRequested(md, supplier));
            if (icon != null) {
                setGraphic(icon);
            }
        }
    }

    /**
     * A specialized menuItem that triggers a constructor call
     * @param <T> the target class
     */
    private class ConstructorMenuItem<T> extends MenuItem {
        public ConstructorMenuItem(Constructor<T> constructor, ImageView icon) {
            super(constructor.toGenericString());
            setOnAction(event -> executeConstructorRequested(constructor));
            if (icon != null) {
                setGraphic(icon);
            }
        }
    }

    /**
     * Called when the user requests the Instanciation of a class through a specific constructor.
     * If the constructor doesn't take any arguments, it is called immediately; otherwise a dialog is
     * displayed to let the user provide the arguments.
     * @param <T>         the target type
     * @param constructor the constructor to call
     */
    private <T> void executeConstructorRequested(Constructor<T> constructor) {
//        System.out.println(constructor.toGenericString());
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
            //FIXME by default we just display the result, should be configurable
            methodCallUI.setOnSuccess(o -> getBuilder().displayInstance(o));
        }
    }

    /**
     * Called when the user requests the execution of a method. If the method doesn't take any arguments,
     * it is executed immediately; otherwise a dialog is displayed to let the user provide the arguments.
     * @param <T> the target type
     * @param <R> the return type of the method
     * @param md the descriptor of the method to execute
     * @param targetSupplier a Supplier used to obtain the target object
     */
    private <T, R> void executeMethodRequested(MethodDescriptor md, Supplier<T> targetSupplier) {
//        System.out.println(md.getName());
        Method method = md.getMethod();

        final T target = targetSupplier.get();
        final Class<R> returnType = (Class<R>) method.getReturnType();
        // if no arg, execute immediately, otherwise display arg dialog
        if (method.getParameterCount() == 0) {
            try {
                R result = (R) method.invoke(target);
                // if the method returns something, display it
                if (returnType != Void.TYPE) {
                    getBuilder().displayInstance(result, returnType, "Result");
                }
                // TODO better handling of returned object
            } catch (Exception e) {
                e.printStackTrace();
                getBuilder().displayException(e);
            }
        } else {
            JfxMethodCallUI methodCallUI = new JfxMethodCallUI(getBuilder(), method);
            getBuilder().display(methodCallUI);
        }
    }

    @SuppressWarnings("unused")
    public void displayObjectRegistry() {
        MapUI<Parent, Identifier, Object> ui = builder.buildMapUI();
        ui.setTarget(ObjectRegistry.INSTANCE.getMap());
        builder.display(ui);
    }

}
