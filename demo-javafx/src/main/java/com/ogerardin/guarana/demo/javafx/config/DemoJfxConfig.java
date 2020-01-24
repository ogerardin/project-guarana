/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.demo.javafx.config;

import com.ogerardin.business.sample.config.model.DeployedWebapp;
import com.ogerardin.business.sample.config.model.Host;
import com.ogerardin.business.sample.config.model.TomcatInstance;
import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.application.Application;
import javafx.stage.Stage;
import lombok.Data;

import java.util.Set;

/**
 * @author Olivier
 * @since 28/05/15
 */
public class DemoJfxConfig extends Application {

    private static World world;

    @Override
    public void start(Stage primaryStage) {

        // instantiate UiManager for JavaFX
        JfxUiManager uiManager = new JfxUiManager();

        // build UI for a DomainManager and bind it to actual instance
        JfxInstanceUI<World> ui = uiManager.buildInstanceUI(World.class);
        ui.bind(world);

        //display UI in JavaFX primary Stage
        uiManager.display(ui, primaryStage, "Hello Guarana!");
    }

    public static void main(String[] args) {

        // instantiate our main business object
        world = new World();

        // handoff to JavaFX; this will call the start() method
        launch(args);
    }

    @Data
    public static class World {
        public Set<Host> hosts;
        public Set<TomcatInstance> tomcatInstances;
        public Set<DeployedWebapp> deployedWebapps;
    }
}

