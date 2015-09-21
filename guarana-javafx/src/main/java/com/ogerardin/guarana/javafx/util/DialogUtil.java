package com.ogerardin.guarana.javafx.util;

import com.ogerardin.guarana.core.ui.InstanceUI;
import com.ogerardin.guarana.core.ui.Renderable;
import com.ogerardin.guarana.javafx.ui.builder.JfxUiBuilder;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by oge on 09/09/2015.
 */
public class DialogUtil {

    public static void showExceptionDialog(Throwable e) {
        final InstanceUI<Parent, Throwable> exceptionInstanceUI = JfxUiBuilder.INSTANCE.buildInstanceUI(Throwable.class);
        exceptionInstanceUI.setTarget(e);
        display(exceptionInstanceUI, "Caught Exception");
    }

    public static void display(Renderable<Parent> renderable, String title) {
        Stage stage = new Stage();
        display(stage, renderable, title);
    }

    public static void display(Stage stage, Renderable<Parent> renderable, String title) {
        stage.setTitle(title);
        Scene scene = new Scene(renderable.render());
        stage.setScene(scene);
        stage.show();
    }

}
