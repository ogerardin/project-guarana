/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.test.runnable;

import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.test.items.ItemJfx;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Date;

public class SingleInstanceWithJfx extends Application {

    private static ItemJfx item;

    @Override
    public void start(Stage primaryStage) {

        // instantiate UiManager for JavaFX
        JfxUiManager uiManager = new JfxUiManager();

        // build UI and bind it to actual instance
        JfxInstanceUI<ItemJfx> ui = uiManager.buildInstanceUI(ItemJfx.class);
        ui.bind(item);

        //display UI in JavaFX primary Stage
        uiManager.display(ui, primaryStage, "Hello Guarana!");
    }

    public static void main(String[] args) {

        // instantiate our main business object
        item = new ItemJfx();
        item.setName("root");
        item.setDate(new Date());

        // handoff to JavaFX; this will call the start() method
        launch(args);
    }
}

