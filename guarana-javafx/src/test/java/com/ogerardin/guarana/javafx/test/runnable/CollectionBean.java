/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.test.runnable;

import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.test.items.ItemBean;
import com.ogerardin.guarana.javafx.ui.JfxCollectionUI;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class CollectionBean extends Application {

    private static List<ItemBean> items;

    @Override
    public void start(Stage primaryStage) {

        // instantiate UiManager for JavaFX
        JfxUiManager uiManager = new JfxUiManager();

        // build UI and bind it to actual instance
        JfxCollectionUI<ItemBean> ui = uiManager.buildCollectionUi(ItemBean.class);
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

