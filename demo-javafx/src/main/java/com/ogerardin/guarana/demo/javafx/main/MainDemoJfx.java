/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.demo.javafx.main;

/**
 * @author Olivier
 * @since 28/05/15
 */

import com.ogerardin.guarana.core.config.Configuration;
import com.ogerardin.guarana.core.ui.InstanceUI;
import com.ogerardin.guarana.demo.javafx.adapters.PersonManagerDb4oImpl;
import com.ogerardin.guarana.demo.model.PersonManager;
import com.ogerardin.guarana.javafx.JfxUiBuilder;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class MainDemoJfx extends Application {

    private static JfxUiBuilder uiBuilder;
    private static PersonManager personManager;

    @Override
    public void start(Stage primaryStage) {

        final InstanceUI<Parent, PersonManager> ui = uiBuilder.buildInstanceUI(PersonManager.class);

        ui.setTarget(personManager);

        JfxUiBuilder.display(ui, primaryStage, "Hello Guarana!");
    }

    public static void main(String[] args) {

        personManager = new PersonManagerDb4oImpl();

        Configuration config = new Configuration();
        config.forClass(Throwable.class).hideProperties("localizedMessage");

        uiBuilder = new JfxUiBuilder(config);

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            uiBuilder.displayException(e);
            System.exit(1);
        });

        launch(args);
    }
}

