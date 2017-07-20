/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.test.runnable;

import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.test.items.ItemPojo;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.application.Application;
import javafx.stage.Stage;

public class SingleInstanceWithPojo extends Application {

    private static ItemPojo item;

    @Override
    public void start(Stage primaryStage) {

        // instantiate UiManager for JavaFX
        JfxUiManager uiManager = new JfxUiManager();

        // build UI and bind it to actual instance
        JfxInstanceUI<ItemPojo> ui = uiManager.buildInstanceUI(ItemPojo.class);
        ui.bind(item);

        //display UI in JavaFX primary Stage
        uiManager.display(ui, primaryStage, "Hello Guarana!");
    }

    public static void main(String[] args) {

        // instantiate our main business object
        item = new ItemPojo();
        item.setName("root");

        // handoff to JavaFX; this will call the start() method
        launch(args);
    }
}

