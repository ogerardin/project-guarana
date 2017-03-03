/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.demo.javafx.domain0;

/**
 * @author Olivier
 * @since 28/05/15
 */

import com.ogerardin.guarana.demo.javafx.domain0.adapters.DomainManagerDb4OImpl;
import com.ogerardin.guarana.domain0.service.DomainManager;
import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.application.Application;
import javafx.stage.Stage;

public class DemoJfx extends Application {

    private static DomainManager domainManager;

    @Override
    public void start(Stage primaryStage) {

        // instantiate UiManager for JavaFX
        JfxUiManager uiManager = new JfxUiManager();

        // build UI for a DomainManager and bind it to actual instance
        JfxInstanceUI<DomainManager> ui = uiManager.buildInstanceUI(DomainManager.class);
        ui.bind(domainManager);

        //display UI in JavaFX primary Stage
        uiManager.display(ui, primaryStage, "Hello Guarana!");
    }

    public static void main(String[] args) {

        // instantiate our main business object
        domainManager = new DomainManagerDb4OImpl();

        // handoff to JavaFX; this will call the start() method
        launch(args);
    }
}

