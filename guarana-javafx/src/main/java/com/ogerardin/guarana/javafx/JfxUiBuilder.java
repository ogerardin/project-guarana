/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx;

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
import org.apache.commons.lang.Validate;

/**
 * UIBuilder implementation for JavaFX. The chosen renderable type is {@link Parent} as it can be the root of a
 * {@link Node} tree and can be put inside a {@link Stage}
 *
 * @author oge
 * @since 07/09/2015
 */
public class JfxUiBuilder implements UIBuilder<Parent> {

    private static final String GUARANA_DEFAULT_CSS = "/guarana-default.css";

    private final Configuration configuration;
    private String defaultStylesheet;

    /**
     * Builds a JfxBuilder with a default configuration
     */
    public JfxUiBuilder() {
        this(new Configuration());
    }

    /**
     * Builds a JfxBuilder with the specified configuration
     */
    public JfxUiBuilder(Configuration configuration) {
        Validate.notNull(configuration);
        this.configuration = configuration;
        this.defaultStylesheet = getClass().getResource(GUARANA_DEFAULT_CSS).toExternalForm();
    }

    public String getDefaultStylesheet() {
        return defaultStylesheet;
    }


    @Override
    public <C> InstanceUI<Parent, C> buildInstanceUI(Class<C> clazz) {
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
            if (parent != null) {
                final Stage finalStage = stage;
                stage.setOnShowing(event -> {
                    Bounds boundsInScreen = parent.localToScreen(parent.getBoundsInLocal());
                    finalStage.setX(boundsInScreen.getMinX());
                    finalStage.setY(boundsInScreen.getMinY());
                });

            }
        }
        if (title != null) {
            stage.setTitle(title);
        }
        Parent root = renderable.render();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getDefaultStylesheet());
        stage.setScene(scene);
        stage.show();
    }

    public <T> void displayInstance(T target) {
        displayInstance(target, (Class<T>) target.getClass(), null, null, null);
    }

    public <T> void displayInstance(T target, Class<T> targetClass, Stage stage, Node parent, String title) {
        // build instanceUI for the target class and display it in stage
        final InstanceUI<Parent, T> ui = buildInstanceUI(targetClass);
        ui.setTarget(target);
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
}
