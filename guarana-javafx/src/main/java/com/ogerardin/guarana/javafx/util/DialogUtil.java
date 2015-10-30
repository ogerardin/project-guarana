/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.util;

import com.ogerardin.guarana.core.ui.InstanceUI;
import com.ogerardin.guarana.core.ui.Renderable;
import com.ogerardin.guarana.javafx.JfxUiBuilder;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author oge
 * @since 09/09/2015
 */
public enum DialogUtil {
    ;

    public static void displayException(Throwable e) {
        final InstanceUI<Parent, Throwable> exceptionInstanceUI = JfxUiBuilder.INSTANCE.buildInstanceUI(Throwable.class);
        exceptionInstanceUI.setTarget(e);
        display(exceptionInstanceUI, "Caught Exception");
    }

    //// display variants

    public static void display(Renderable<Parent> renderable, Stage stage, Node parent, String title) {
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
        stage.setScene(scene);
        stage.show();
    }

    public static void display(Renderable<Parent> renderable, Node parent, String title) {
        display(renderable, null, parent, title);
    }

    public static void display(Renderable<Parent> renderable, String title) {
        display(renderable, null, null, title);
    }

    public static void display(Renderable<Parent> renderable, Stage stage, String title) {
        display(renderable, stage, null, title);
    }

    public static void display(Renderable<Parent> renderable, Stage stage) {
        display(renderable, stage, null, null);
    }

    public static void display(Renderable<Parent> renderable, Node parent) {
        display(renderable, null, parent, null);
    }

    public static void display(Renderable<Parent> renderable) {
        display(renderable, null, null, null);
    }


    //// displayInstance variants

    public static <T> void displayInstance(T target, Class<T> targetClass, Stage stage, Node parent, String title) {
        // build instanceUI for the target class and display it in stage
        final InstanceUI<Parent, T> ui = JfxUiBuilder.INSTANCE.buildInstanceUI(targetClass);
        ui.setTarget(target);
        display(ui, stage, parent, title);
    }

    public static <T> void displayInstance(T target) {
        displayInstance(target, (Class<T>) target.getClass(), null, null, null);
    }

    public static <T> void displayInstance(T target, Class<T> targetClass, Node parent) {
        displayInstance(target, targetClass, null, parent, null);
    }

    public static <T> void displayInstance(T target, Class<T> targetClass) {
        displayInstance(target, targetClass, null, null, null);
    }

    public static <T> void displayInstance(T target, Class<T> targetClass, Stage stage) {
        displayInstance(target, targetClass, stage, null, null);
    }

    public static <T> void displayInstance(T target, Class<T> targetClass, Node parent, String title) {
        displayInstance(target, targetClass, null, parent, title);
    }

    public static <T> void displayInstance(T target, Class<T> targetClass, String title) {
        displayInstance(target, targetClass, null, null, title);
    }

    public static <T> void displayInstance(T target, Class<T> targetClass, Stage stage, String title) {
        displayInstance(target, targetClass, stage, null, title);
    }

}
