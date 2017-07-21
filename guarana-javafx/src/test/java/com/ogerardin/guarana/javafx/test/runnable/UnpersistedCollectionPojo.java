/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.test.runnable;

import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.test.items.ItemPojo;
import com.ogerardin.guarana.javafx.ui.JfxCollectionUI;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class UnpersistedCollectionPojo extends Application {

    private static List<ItemPojo> items;

    @Override
    public void start(Stage primaryStage) {

        // instantiate UiManager for JavaFX
        JfxUiManager uiManager = new JfxUiManager();

        // build UI and bind it to actual instance
        JfxCollectionUI<ItemPojo> ui = uiManager.buildCollectionUi(ItemPojo.class);
        ui.bind(items);

        //display UI in JavaFX primary Stage
        uiManager.display(ui, primaryStage, "Hello Guarana!");
    }

    public static void main(String[] args) {

        // instantiate our main business object
        items = new ArrayList<>();

        // handoff to JavaFX; this will call the start() method
        launch(args);
    }
}

