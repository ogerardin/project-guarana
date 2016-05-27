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

        JfxInstanceUI<DemoManager> ui = uiManager.buildInstanceUI(DemoManager.class);

        ui.setTarget(demoManager);

        uiManager.display(ui, primaryStage, "Hello Guarana!");
    }

    public static void main(String[] args) {

        demoManager = new DemoManagerDb4OImpl();

        Configuration config = new Configuration();
        config.forClass(Throwable.class).hideProperties("localizedMessage");
        config.forClass(Person.class)
                .hideProperties("fullNameLastFirst", "fullNameFirstLast")
                .setToString(Person::getFullNameLastFirst);
        config.forClass(Date.class).setEmbeddedUiClass(JfxDateUi.class);

        uiManager = new JfxUiManager(config);

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            uiManager.displayException(e);
            System.exit(1);
        });

        launch(args);
    }
}

