/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.demo.javafx.main;

/**
 * @author Olivier
 * @since 28/05/15
 */

import com.ogerardin.guarana.demo.javafx.adapters.DemoManagerDb4OImpl;
import com.ogerardin.guarana.demo.model.DemoManager;
import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainDemoJfx extends Application {

    private static DemoManager demoManager;

    @Override
    public void start(Stage primaryStage) {

        // instantiate UiManager for JavaFX
        JfxUiManager uiManager = new JfxUiManager();

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

        // handoff to JavaFX; this will call the start() method
        launch(args);
    }
}

