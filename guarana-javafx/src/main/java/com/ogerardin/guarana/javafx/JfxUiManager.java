/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.ogerardin.guarana.core.config.ClassConfiguration;
import com.ogerardin.guarana.core.config.Configuration;
import com.ogerardin.guarana.core.ui.Renderable;
import com.ogerardin.guarana.javafx.ui.*;
import com.ogerardin.guarana.javafx.ui.impl.DefaultJfxCollectionUI;
import com.ogerardin.guarana.javafx.ui.impl.DefaultJfxEmbeddedInstanceUI;
import com.ogerardin.guarana.javafx.ui.impl.DefaultJfxInstanceUI;
import com.ogerardin.guarana.javafx.ui.impl.DefaultJfxMapUI;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;
import org.apache.commons.lang.Validate;

import java.util.function.Consumer;

/**
 * UIBuilder implementation for JavaFX. The chosen renderable type is {@link Parent} as it can be the root of a
 * {@link Node} tree and can be put inside a {@link Stage}
 *
 * @author oge
 * @since 07/09/2015
 */
//TOTO split Builder from Manager
public class JfxUiManager implements JfxUIBuilder {


    private static final String GUARANA_DEFAULT_CSS = "/guarana-default.css";

    private BiMap<Pair<Class, Object>, Renderable> objectRenderableMap = HashBiMap.create();
    private BiMap<Renderable, Stage> renderableStageMap = HashBiMap.create();

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
        Class<?> uiClass = classConfiguration.getUiClass();
        if (uiClass == null) {
            return new DefaultJfxInstanceUI<>(this, clazz);
        }
        try {
            // might throw ClassCastException if the specified class doesn't match JfxInstanceUI
            return (JfxInstanceUI<C>) uiClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <C> JfxInstanceUI<C> buildEmbeddedInstanceUI(Class<C> clazz) {
        ClassConfiguration<C> classConfiguration = configuration.forClass(clazz);
        Class<?> uiClass = classConfiguration.getEmbeddedUiClass();
        if (uiClass == null) {
            return new DefaultJfxEmbeddedInstanceUI<>(this, clazz);
        }
        try {
            // might throw ClassCastException if the specified class doesn't match JfxInstanceUI
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
        e.printStackTrace();
        JfxInstanceUI<Throwable> exceptionInstanceUI = buildInstanceUI(Throwable.class);
        exceptionInstanceUI.setTarget(e);
        display(exceptionInstanceUI, "Caught Exception");
    }


    public void display(JfxRenderable renderable, Node parent, String title) {
        display(renderable, null, parent, title);
    }

    public void display(JfxRenderable renderable, Stage stage, String title) {
        display(renderable, stage, null, title);
    }

    public void display(JfxRenderable renderable, Stage stage) {
        display(renderable, stage, null, null);
    }

    public void display(JfxRenderable renderable, Node parent) {
        display(renderable, null, parent, null);
    }

    public void display(JfxRenderable renderable) {
        display(renderable, null, null, null);
    }

    public void display(JfxRenderable renderable, String title) {
        display(renderable, null, null, title);
    }

    public void display(JfxRenderable renderable, Stage stage, Node parent, String title) {
        if (stage == null) {
            stage = new Stage();
        }
        if (parent != null) {
            positionRelativeToParent(stage, parent);
        }
        if (title != null) {
            stage.setTitle(title);
        }
        Parent root = renderable.render();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getDefaultStylesheet());
        stage.setScene(scene);

        renderableStageMap.put(renderable, stage);
        stage.show();
    }

    private void positionRelativeToParent(final Window window, Node parent) {
        window.setOnShowing(event -> {
            Bounds boundsInScreen = parent.localToScreen(parent.getBoundsInLocal());
            window.setX(boundsInScreen.getMinX());
            window.setY(boundsInScreen.getMinY());
        });
    }

    public <T> void displayInstance(T target) {
        displayInstance(target, (Class<T>) target.getClass(), null, null, null);
    }

    public <T> void displayInstance(T target, Class<T> targetClass, Stage stage, Node parent, String title) {
        // check if target already has a UI
        Pair<Class, Object> key = new Pair<>(targetClass, target);
        Renderable renderable = objectRenderableMap.get(key);
        if (renderable != null) {
            show(renderable);
            return;
        }
        // build instanceUI for the target class and display it in stage
        JfxInstanceUI<T> ui = buildInstanceUI(targetClass);
        ui.setTarget(target);
        objectRenderableMap.put(key, ui);
        display(ui, stage, parent, title);
    }


    public <T> void displayInstance(T target, Class<T> targetClass, Node parent) {
        displayInstance(target, targetClass, null, parent, null);
    }

    public <T> void displayInstance(T target, Class<T> targetClass) {
        displayInstance(target, targetClass, null, null, null);
    }

    public <T> void displayInstance(T target, Class<T> targetClass, Stage stage) {
        displayInstance(target, targetClass, stage, null, null);
    }

    public <T> void displayInstance(T target, Class<T> targetClass, Node parent, String title) {
        displayInstance(target, targetClass, null, parent, title);
    }

    public <T> void displayInstance(T target, Class<T> targetClass, String title) {
        displayInstance(target, targetClass, null, null, title);
    }

    public <T> void displayInstance(T target, Class<T> targetClass, Stage stage, String title) {
        displayInstance(target, targetClass, stage, null, title);
    }

    public Configuration getConfiguration() {
        return configuration;
    }


    private void stageAction(Renderable renderable, Consumer<Stage> stageAction) {
        Stage stage = renderableStageMap.get(renderable);
        if (stage == null) {
            System.err.println("WARNING: can't find stage for the specified renderable; maybe it was never displayed?");
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

}
