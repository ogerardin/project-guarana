/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.ogerardin.guarana.core.config.ClassConfiguration;
import com.ogerardin.guarana.core.config.Configuration;
import com.ogerardin.guarana.core.ui.InstanceUI;
import com.ogerardin.guarana.core.ui.Renderable;
import com.ogerardin.guarana.javafx.ui.*;
import com.ogerardin.guarana.javafx.ui.impl.DefaultJfxCollectionUI;
import com.ogerardin.guarana.javafx.ui.impl.DefaultJfxInstanceUI;
import com.ogerardin.guarana.javafx.ui.impl.DefaultJfxMapUI;
import com.ogerardin.guarana.javafx.ui.impl.embedded.DefaultJfxEmbeddedInstanceUI;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.Validate;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * UIBuilder implementation for JavaFX. The chosen renderable type is {@link Parent}
 *
 * @author oge
 * @since 07/09/2015
 */
//TODO split Builder from Manager
@Slf4j
public class JfxUiManager implements JfxUIBuilder {

    private static final String GUARANA_DEFAULT_CSS = "/guarana-default.css";

    private final BiMap<Object, Renderable> objectRenderableMap = HashBiMap.create();
    private final BiMap<Renderable, Stage> renderableStageMap = HashBiMap.create();

    private final Configuration configuration;
    private String defaultStylesheet;

    /**
     * Builds a JfxBuilder with a default configuration
     */
    public JfxUiManager() {
        this(new Configuration());
    }

    /**
     * Builds a JfxBuilder with the specified configuration
     */
    public JfxUiManager(Configuration configuration) {
        Validate.notNull(configuration);
        this.configuration = configuration;
        this.defaultStylesheet = getClass().getResource(GUARANA_DEFAULT_CSS).toExternalForm();
    }

    public String getDefaultStylesheet() {
        return defaultStylesheet;
    }


    @Override
    public <C> JfxInstanceUI<C> buildInstanceUI(Class<C> clazz) {
        ClassConfiguration<C> classConfiguration = configuration.forClass(clazz);
        // if the configuration specifies a custom UI class for this class, use it, otherwise use
        // DefaultJfxInstanceUI
        Class<? extends InstanceUI<?, C>> uiClass = classConfiguration.getUiClass();
        if (uiClass == null) {
            return new DefaultJfxInstanceUI<>(this, clazz);
        }
        try {
            // might throw ClassCastException if the specified class doesn't implement JfxInstanceUI
            //noinspection unchecked
            return (JfxInstanceUI<C>) uiClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public <C> JfxInstanceUI<C> buildEmbeddedInstanceUI(Class<C> clazz) {
        ClassConfiguration<C> classConfiguration = configuration.forClass(clazz);
        // if the configuration specifies a custom embedded UI class for this class, use it, otherwise use
        // DefaultJfxEmbeddedInstanceUI
        Class<? extends InstanceUI<?, C>> uiClass = classConfiguration.getEmbeddedUiClass();
        if (uiClass == null) {
            return new DefaultJfxEmbeddedInstanceUI<C>(this, clazz);
        }
        try {
            // might throw ClassCastException if the specified class doesn't implement JfxInstanceUI
            //noinspection unchecked
            return (JfxInstanceUI<C>) uiClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public <C> JfxCollectionUI<C> buildCollectionUi(Class<C> itemClass) {
        return new DefaultJfxCollectionUI<>(this, itemClass);
    }

    @Override
    public <K, V> JfxMapUI<K, V> buildMapUI() {
        return new DefaultJfxMapUI<>(this);
    }


    //
    // utility methods
    //

    public void displayException(Throwable e) {
        //TODO improve this
        log.error("Displaying exception", e);
        JfxInstanceUI<Throwable> exceptionInstanceUI = buildInstanceUI(Throwable.class);
        exceptionInstanceUI.bind(e);
        display(exceptionInstanceUI, "Caught Exception");
    }


    public JfxRenderable display(JfxRenderable renderable, Node parent, String title) {
        return display(renderable, null, parent, title);
    }

    public JfxRenderable display(JfxRenderable renderable, Stage stage, String title) {
        return display(renderable, stage, null, title);
    }

    public JfxRenderable display(JfxRenderable renderable) {
        return display(renderable, null, null, null);
    }

    public JfxRenderable display(JfxRenderable renderable, String title) {
        return display(renderable, null, null, title);
    }

    public JfxRenderable display(JfxRenderable renderable, Stage stage, Node parent, String title) {
        if (stage == null) {
            stage = new Stage();
        }
        if (parent != null) {
            positionRelativeToParent(stage, parent);
        }
        if (title != null) {
            stage.setTitle(title);
        }
        Parent root = renderable.getRendered();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getDefaultStylesheet());
        stage.setScene(scene);

        renderableStageMap.put(renderable, stage);
        stage.show();
        return renderable;
    }

    private void positionRelativeToParent(final Window window, Node parent) {
        window.setOnShowing(event -> {
            Bounds boundsInScreen = parent.localToScreen(parent.getBoundsInLocal());
            window.setX(boundsInScreen.getMinX());
            window.setY(boundsInScreen.getMinY());
        });
    }

    public <T> JfxInstanceUI<T> displayInstance(T target) {
        return displayInstance(target, (Class<T>) target.getClass(), null, null, null);
    }

    public <T> JfxInstanceUI<T> displayInstance(T target, Class<T> targetClass, Stage stage, Node parent, String title) {
        // check if target already has a UI
        Object key = getUiKey(target);
        JfxInstanceUI<T> ui = (JfxInstanceUI<T>) objectRenderableMap.get(key);
        if (ui != null) {
            log.debug("Showing existing UI for key {}", key);
            show(ui);
        } else {
            log.debug("Key {} not found, building UI", key);
            // build instanceUI for the target class and display it in stage
            ui = buildInstanceUI(targetClass);
            ui.bind(target);
            objectRenderableMap.put(key, ui);
            display(ui, stage, parent, title);
        }
        return ui;
    }

    public <C> JfxCollectionUI<C> displayCollection(Collection<C> collection, Class<C> itemClass, Node parent, String title) {
        // check if target already has a UI
        Object key = getUiKey(collection);
        JfxCollectionUI<C> ui = (JfxCollectionUI<C>) objectRenderableMap.get(key);
        if (ui != null) {
            show(ui);
        } else {
            // build UI for the target collection class and display it in stage
            ui = buildCollectionUi(itemClass);
            ui.bind(collection);
            objectRenderableMap.put(key, ui);
            display(ui, parent, title);
        }
        return ui;
    }

    public <C> JfxCollectionUI<C> displayArray(C[] array, Class<C> itemClass, Node parent, String title) {
        // check if target already has a UI
        Object key = getUiKey(array);
        JfxCollectionUI<C> ui = (JfxCollectionUI<C>) objectRenderableMap.get(key);
        if (ui != null) {
            show(ui);
        } else {
            // build UI for the target collection class and display it in stage
            ui = buildCollectionUi(itemClass);
            ui.bind(Arrays.asList(array));
            objectRenderableMap.put(key, ui);
            display(ui, parent, title);
        }
        return ui;


    }


    public <T> JfxInstanceUI<T> displayInstance(T target, Class<T> targetClass, Node parent, String title) {
        return displayInstance(target, targetClass, null, parent, title);
    }

    public <T> JfxInstanceUI<T> displayInstance(T target, Class<T> targetClass, String title) {
        return displayInstance(target, targetClass, null, null, title);
    }

    public Configuration getConfiguration() {
        return configuration;
    }


    private void stageAction(Renderable renderable, Consumer<Stage> stageAction) {
        Stage stage = renderableStageMap.get(renderable);
        if (stage == null) {
            log.warn("Can't find stage for the specified renderable; maybe it was never displayed?");
            return;
        }
        stageAction.accept(stage);
    }

    public void hide(Renderable renderable) {
        stageAction(renderable, Window::hide);
    }

    public void show(Renderable renderable) {
        stageAction(renderable, stage -> {
            stage.show();
            stage.toFront();
        });
    }

    private <T> Object getUiKey(T target) {
        return System.identityHashCode(target);
    }


}
