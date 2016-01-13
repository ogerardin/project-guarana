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
import com.ogerardin.guarana.demo.javafx.adapters.DemoManagerDb4OImpl;
import com.ogerardin.guarana.demo.javafx.ui.JfxDateUi;
import com.ogerardin.guarana.demo.model.DemoManager;
import com.ogerardin.guarana.demo.model.Person;
import com.ogerardin.guarana.javafx.JfxUiBuilder;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.util.Date;

public class MainDemoJfx extends Application {

    private static JfxUiBuilder uiBuilder;
    private static DemoManager demoManager;

    @Override
    public void start(Stage primaryStage) {

        InstanceUI<Parent, DemoManager> ui = uiBuilder.buildInstanceUI(DemoManager.class);

        ui.setTarget(demoManager);

        uiBuilder.display(ui, primaryStage, "Hello Guarana!");
    }

    public static void main(String[] args) {

        demoManager = new DemoManagerDb4OImpl();

        Configuration config = new Configuration();
        config.forClass(Throwable.class).hideProperties("localizedMessage");
        config.forClass(Person.class).hideProperties("fullNameLastFirst", "fullNameFirstLast");
        config.forClass(Person.class).setToString(Person::getFullNameLastFirst);
        config.forClass(Date.class).setUiClass(JfxDateUi.class);

        uiBuilder = new JfxUiBuilder(config);

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            uiBuilder.displayException(e);
            System.exit(1);
        });

        launch(args);
    }
}

