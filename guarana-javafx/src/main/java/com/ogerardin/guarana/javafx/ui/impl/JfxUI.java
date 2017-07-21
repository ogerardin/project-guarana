/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui.impl;

import com.ogerardin.guarana.core.config.Configuration;
import com.ogerardin.guarana.core.metamodel.ClassInformation;
import com.ogerardin.guarana.core.metamodel.ExecutableInformation;
import com.ogerardin.guarana.core.registry.Identifier;
import com.ogerardin.guarana.core.registry.ObjectRegistry;
import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.ui.JfxMapUI;
import com.ogerardin.guarana.javafx.ui.JfxRenderable;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Common abstract superclass for Java FX renderable classes.
 *
 * @author oge
 * @since 24/09/2015
 */
abstract class JfxUI implements JfxRenderable {

    private static final Logger LOGGER = LoggerFactory.getLogger(JfxUI.class);

    Image ICON_CONSTRUCTOR = new Image("icons/call_class_16.png");
    Image ICON_METHOD = new Image("icons/call_method_16.png");
    Image ICON_DRAG_HANDLE = new Image("icons/drag_handle_16.jpg");

    private final JfxUiManager builder;

    JfxUI(JfxUiManager builder) {
        Validate.notNull(builder);
        this.builder = builder;
    }

    static Font getTitleLabelFont() {
        return Font.font("Tahoma", FontWeight.NORMAL, 20);
    }

    <T> void configureContextMenu(Control control, ClassInformation<T> classInformation, Supplier<T> targetSupplier) {
        ContextMenu contextMenu = new ContextMenu();
        final Class<T> targetClass = classInformation.getJavaClass();

        // add instance methods
        if (targetSupplier != null) {
            classInformation.getMethods().stream()
                    .filter(methodInfo -> !methodInfo.isGetterOrSetter())
                    .filter(methodInfo -> !getConfiguration().isHidden(targetClass, methodInfo.getExecutable()))
                    .map(methodInfo -> new ActionMenuItem(methodInfo, targetSupplier))
                    .forEach(menuItem -> contextMenu.getItems().add(menuItem));
        }

        // add constructors
        contextMenu.getItems().add(new SeparatorMenuItem());
        classInformation.getConstructors().stream()
                .map(constructor -> new ActionMenuItem(constructor))
                .forEach(menuItem -> contextMenu.getItems().add(menuItem));
        control.setContextMenu(contextMenu);

        // add contributed methods
        // FIXME contributed methods do not belong to the beanClass and hence cannot use targetSupplier !
//        contextMenu.getItems().add(new SeparatorMenuItem());
//
//        targetClassInformation.getContributedExecutables().stream()
//                .filter(ExecutableInformation::isMethod)
//                .map(executable -> new ActionMenuItem(executable, null))
//                .forEach(menuItem -> contextMenu.getItems().add(menuItem));


        // add other items
        // FIXME disabled for now because it triggers too much introspection
/*
        BeanInfoIntrospector.getClassInformation(this.getClass()).getMethods().stream()
                .filter(m -> m.getName().equals("displayObjectRegistry"))
                .map(m -> new MethodMenuItem<>(m, () -> this, null))
                .forEach(menuItem -> contextMenu.getItems().add(menuItem));
*/
    }

    <T> void configureDragSource(Node source, Supplier<T> valueSupplier) {
        source.setOnDragDetected(event -> {
            Dragboard dragboard = source.startDragAndDrop(TransferMode.LINK);
            ClipboardContent content = new ClipboardContent();
            T value = valueSupplier.get();
            Identifier identifier = ObjectRegistry.INSTANCE.put(value);
            content.put(Const.DATA_FORMAT_OBJECT_IDENTIFIER, identifier);
            dragboard.setContent(content);
            event.consume();
        });
        // drag done: nothing to do, just consume the event
        source.setOnDragDone(Event::consume);
    }

    <T> void configureDropTarget(Node control, Predicate<T> valueValidator, Consumer<T> valueConsumer) {
        // Note: it would make sense to accept the transfer in the DragEntered handler (which is called once when the
        // pointer enters the target), but for some reason it doesn't work, you have to do it in the DragOver handler
        // (which is called whenever the pointer moves inside the target)
        control.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasContent(Const.DATA_FORMAT_OBJECT_IDENTIFIER)) {
                // retrieve identifier from dragboard and associated source object in registry
                Identifier identifier = (Identifier) db.getContent(Const.DATA_FORMAT_OBJECT_IDENTIFIER);
                T value = (T) ObjectRegistry.INSTANCE.get(identifier);
                if (value == null) {
                    LOGGER.error("Key not found in object registry: " + identifier);
                    return;
                }
                if (valueValidator.test(value)) {
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
                T value = (T) ObjectRegistry.INSTANCE.get(identifier);
                if (value == null) {
                    LOGGER.error("Key not found in object registry: " + identifier);
                    return;
                }
                boolean completed = false;
                try {
                    valueConsumer.accept(value);
                    completed = true;
                } catch (Exception e) {
                    getBuilder().displayException(e);
                }
                event.setDropCompleted(completed);
            }
            event.consume();
        });
    }

    public JfxUiManager getBuilder() {
        return builder;
    }

    protected Configuration getConfiguration() {
        return getBuilder().getConfiguration();
    }


    private class ActionMenuItem<T> extends MenuItem {
        public ActionMenuItem(ExecutableInformation executableInformation) {
            this(executableInformation, null);
        }

        public ActionMenuItem(ExecutableInformation executableInformation, Supplier<T> supplier) {
            this(executableInformation, supplier,
                    executableInformation.isConstructor() ? new ImageView(ICON_CONSTRUCTOR) : new ImageView(ICON_METHOD));
        }

        public ActionMenuItem(ExecutableInformation executableInformation, Supplier<T> supplier, ImageView icon) {
            super(executableInformation.getDefaultLabel(), icon);
            setOnAction(
                    event -> Platform.runLater(() -> {
                        final Executable executable = executableInformation.getExecutable();
                        if (executableInformation.isConstructor()) {
                            executeConstructorRequested((Constructor<?>) executable);
                        } else {
                            executeMethodRequested((Method) executable, supplier);
                        }
                    })
            );
        }

    }




    /**
     * Called when the user requests the Instanciation of a class through a specific constructor.
     * If the constructor doesn't take any arguments, it is called immediately; otherwise a dialog is
     * displayed to let the user provide the arguments.
     *
     * @param <T>         the target type
     * @param constructor the constructor to call
     */
    private <T> void executeConstructorRequested(Constructor<T> constructor) {
        if (constructor.getParameterCount() == 0) {
            invokeNowParameterless(constructor);
        } else {
            JfxExecutableInvocationUI<T, T> methodCallUI = new JfxExecutableInvocationUI(getBuilder(), constructor);
            getBuilder().display(methodCallUI);
            //FIXME by default we just display the result, should be configurable
            methodCallUI.setOnSuccess(o -> getBuilder().displayInstance(o));
        }
    }

    private <T> void invokeNowParameterless(Constructor<T> constructor) {
        Validate.isTrue(constructor.getParameterCount() == 0);
        try {
            T instance = constructor.newInstance();
            getBuilder().displayInstance(instance, constructor.getDeclaringClass(), "New Instance");
        } catch (Exception e) {
            e.printStackTrace();
            getBuilder().displayException(e);
        }
    }

    /**
     * Called when the user requests the execution of a method. If the method doesn't take any arguments,
     * it is executed immediately; otherwise a dialog is displayed to let the user provide the arguments.
     *  @param <T>            the target type
     * @param <R>            the return type of the method
     * @param method             the descriptor of the method to execute
     * @param targetSupplier a Supplier used to obtain the target object (or null if method is static)
     */
    private <T, R> void executeMethodRequested(Method method, Supplier<T> targetSupplier) {
        final T target = targetSupplier != null ? targetSupplier.get() : null;
        final Class<R> returnType = (Class<R>) method.getReturnType();
        // if no arg, execute immediately, otherwise display arg dialog
        if (method.getParameterCount() == 0) {
            invokeNowParameterless(method, target, returnType);
        } else {
            JfxExecutableInvocationUI<T, R> methodCallUI = new JfxExecutableInvocationUI(getBuilder(), method);
            methodCallUI.setContext(target);
            getBuilder().display(methodCallUI);
            //FIXME by default we just display the result, should be configurable
            methodCallUI.setOnSuccess(o -> getBuilder().displayInstance(o));
        }
    }

    private <T, R> void invokeNowParameterless(Method method, T target, Class<R> returnType) {
        Validate.isTrue(method.getParameterCount() == 0);
        try {
            R result = (R) method.invoke(target);
            // if the method returns something, display it
            // TODO better handling of returned object
            if (returnType != Void.TYPE) {
                getBuilder().displayInstance(result, returnType, "Result");
            }
        } catch (Exception e) {
            e.printStackTrace();
            getBuilder().displayException(e);
        }
    }

    @SuppressWarnings("unused")
    public void displayObjectRegistry() {
        JfxMapUI<Identifier, Object> ui = builder.buildMapUI();
        ui.bind(ObjectRegistry.INSTANCE.getMap());
        builder.display(ui);
    }

}
