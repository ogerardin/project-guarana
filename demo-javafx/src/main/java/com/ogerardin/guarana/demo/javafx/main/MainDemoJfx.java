/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.demo.javafx.main;

/**
 * @author Olivier
 * @since 28/05/15
 */

import com.ogerardin.guarana.core.config.Configuration;
import com.ogerardin.guarana.demo.javafx.adapters.DemoManagerDb4OImpl;
import com.ogerardin.guarana.demo.javafx.ui.JfxDateUi;
import com.ogerardin.guarana.demo.model.DemoManager;
import com.ogerardin.guarana.demo.model.Person;
import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Date;

public class MainDemoJfx extends Application {

    private static JfxUiManager uiManager;
    private static DemoManager demoManager;

    @Override
    public void start(Stage primaryStage) {

        // build UI for a DemoManager
        JfxInstanceUI<DemoManager> ui = uiManager.buildInstanceUI(DemoManager.class);

        // populate UI with actual instance
        ui.setTarget(demoManager);

        //display UI in primary Stage
        uiManager.display(ui, primaryStage, "Hello Guarana!");
    }

    public static void main(String[] args) {

        // instantiate our main business object
        demoManager = new DemoManagerDb4OImpl();

        // some configuration
        Configuration config = new Configuration();
        config.forClass(Throwable.class).hideProperties("localizedMessage");
        config.forClass(Person.class)
                .hideProperties("fullNameLastFirst", "fullNameFirstLast")
                .setToString(Person::getFullNameLastFirst);
        config.forClass(Date.class).setEmbeddedUiClass(JfxDateUi.class);
        config.forClass(Object.class).hideAllMethods();

        // instantiate UiManager for JavaFX
        uiManager = new JfxUiManager(config);

        // set a handler for uncaught exceptions that will use the UiManager to display the exception
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            uiManager.displayException(e);
            System.exit(1);
        });

        // handoff to JavaFX; this will call the start() method
        launch(args);
    }
}

