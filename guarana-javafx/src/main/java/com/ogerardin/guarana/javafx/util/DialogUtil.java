package com.ogerardin.guarana.javafx.util;

import com.ogerardin.guarana.core.ui.InstanceUI;
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
        Stage stage = new Stage();
        stage.setTitle("Caught Exception");
        Scene scene = new Scene(exceptionInstanceUI.render());
        stage.setScene(scene);
        stage.show();
    }
}
