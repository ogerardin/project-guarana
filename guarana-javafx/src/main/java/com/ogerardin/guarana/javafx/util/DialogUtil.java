package com.ogerardin.guarana.javafx.util;

import com.ogerardin.guarana.core.ui.InstanceUI;
import com.ogerardin.guarana.core.ui.Renderable;
import com.ogerardin.guarana.javafx.JfxUiBuilder;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by oge on 09/09/2015.
 */
public class DialogUtil {

    public static void displayException(Throwable e) {
        final InstanceUI<Parent, Throwable> exceptionInstanceUI = JfxUiBuilder.INSTANCE.buildInstanceUI(Throwable.class);
        exceptionInstanceUI.setTarget(e);
        display(exceptionInstanceUI, "Caught Exception");
    }

    public static void display(Renderable<Parent> renderable) {
        Stage stage = new Stage();
        display(renderable, stage);
    }
    public static void display(Renderable<Parent> renderable, String title) {
        Stage stage = new Stage();
        display(renderable, stage, title);
    }

    public static void display(Renderable<Parent> renderable, Stage stage) {
        Scene scene = new Scene(renderable.render());
        stage.setScene(scene);
        stage.show();
    }

    public static void display(Renderable<Parent> renderable, Stage stage, String title) {
        stage.setTitle(title);
        display(renderable, stage);
    }

    public static <T> void displayInstance(T target) {
        displayInstance((Class<T>) target.getClass(), target);
    }

    public static <T> void displayInstance(T target, String title) {
        displayInstance((Class<T>) target.getClass(), target, title);
    }

    public static <T> void displayInstance(Class<T> targetClass, T target, String title) {
        Stage stage = new Stage();
        displayInstance(targetClass, target, stage, title);
    }

    public static <T> void displayInstance(T target, Stage stage) {
        displayInstance((Class<T>) target.getClass(), target, stage);
    }

    public static <T> void displayInstance(Class<T> targetClass, T target) {
        Stage stage = new Stage();
        displayInstance(targetClass, target, stage);
    }

    public static <T> void displayInstance(Class<T> targetClass, T target, Stage stage) {
        final InstanceUI<Parent, T> ui = JfxUiBuilder.INSTANCE.buildInstanceUI(targetClass);
        ui.setTarget(target);
        display(ui, stage);

    }

    public static <T> void displayInstance(Class<T> targetClass, T target, Stage stage, String title) {
        stage.setTitle(title);
        displayInstance(targetClass, target, stage);
    }
}
