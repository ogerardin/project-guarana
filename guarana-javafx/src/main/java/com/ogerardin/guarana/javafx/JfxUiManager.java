/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.ogerardin.guarana.core.config.ClassConfiguration;
import com.ogerardin.guarana.core.config.Configuration;
import com.ogerardin.guarana.core.ui.*;
import com.ogerardin.guarana.javafx.ui.JfxClassUI;
import com.ogerardin.guarana.javafx.ui.JfxCollectionUI;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import com.ogerardin.guarana.javafx.ui.JfxMapUI;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.commons.lang.Validate;

import java.util.function.Consumer;

/**
 * UIBuilder implementation for JavaFX. The chosen renderable type is {@link Parent} as it can be the root of a
 * {@link Node} tree and can be put inside a {@link Stage}
 *
 * @author oge
 * @since 07/09/2015
 */
public class JfxUiManager implements UIBuilder<Parent> {


    private static final String GUARANA_DEFAULT_CSS = "/guarana-default.css";

    private BiMap<Object, Renderable> objectRenderableMap = HashBiMap.create();
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
    public <C> InstanceUI<Parent, C> buildInstanceUI(Class<C> clazz) {
        ClassConfiguration<C> classConfiguration = configuration.forClass(clazz);
        Class<InstanceUI<Parent, C>> uiClass = classConfiguration.getUiClass();
        if (uiClass != null) {
            try {
                return uiClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return new JfxInstanceUI<>(this, clazz);
    }

    @Override
    public ClassUI<Parent> buildClassUI(Class clazz) {
        return new JfxClassUI(this, clazz);
    }

    @Override
    public <C> CollectionUI<Parent, C> buildCollectionUi(Class<C> itemClass) {
        return new JfxCollectionUI<>(this, itemClass);
    }

    @Override
    public <K, V> MapUI<Parent, K, V> buildMapUI() {
        return new JfxMapUI<>(this);
    }


    //
    // utility methods
    //

    public void displayException(Throwable e) {
        e.printStackTrace();
        final InstanceUI<Parent, Throwable> exceptionInstanceUI = buildInstanceUI(Throwable.class);
        exceptionInstanceUI.setTarget(e);
        display(exceptionInstanceUI, "Caught Exception");
    }


    public void display(Renderable<Parent> renderable, Node parent, String title) {
        display(renderable, null, parent, title);
    }

    public void display(Renderable<Parent> renderable, Stage stage, String title) {
        display(renderable, stage, null, title);
    }

    public void display(Renderable<Parent> renderable, Stage stage) {
        display(renderable, stage, null, null);
    }

    public void display(Renderable<Parent> renderable, Node parent) {
        display(renderable, null, parent, null);
    }

    public void display(Renderable<Parent> renderable) {
        display(renderable, null, null, null);
    }

    public void display(Renderable<Parent> renderable, String title) {
        display(renderable, null, null, title);
    }

    public void display(Renderable<Parent> renderable, Stage stage, Node parent, String title) {
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
        Renderable renderable = objectRenderableMap.get(target);
        if (renderable != null) {
            show(renderable);
            return;
        }
        // build instanceUI for the target class and display it in stage
        final InstanceUI<Parent, T> ui = buildInstanceUI(targetClass);
        ui.setTarget(target);
        objectRenderableMap.put(target, ui);
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
