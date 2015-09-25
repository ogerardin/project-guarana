package com.ogerardin.guarana.testapp.main;

/**
 * Created by Olivier on 28/05/15.
 */

import com.ogerardin.guarana.javafx.util.DialogUtil;
import com.ogerardin.guarana.testapp.adapters.PersonManagerDb4oImpl;
import com.ogerardin.guarana.testapp.model.PersonManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class ShortDemoJfx extends Application {

    @Override
    public void start(Stage primaryStage) {

        final PersonManager personManager = new PersonManagerDb4oImpl();

        DialogUtil.displayInstance(PersonManager.class, personManager, primaryStage, "Hello, Guarana!");
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace(System.err);
            DialogUtil.displayException(e);
        });
        launch(args);
    }
}

