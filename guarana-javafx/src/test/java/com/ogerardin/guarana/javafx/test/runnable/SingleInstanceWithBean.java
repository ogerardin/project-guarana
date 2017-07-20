/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.test.runnable;

import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.test.items.ItemBean;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.application.Application;
import javafx.stage.Stage;

public class SingleInstanceWithBean extends Application {

    private static ItemBean item;

    @Override
    public void start(Stage primaryStage) {

        // instantiate UiManager for JavaFX
        JfxUiManager uiManager = new JfxUiManager();

        // build UI and bind it to actual instance
        JfxInstanceUI<ItemBean> ui = uiManager.buildInstanceUI(ItemBean.class);
        ui.bind(item);

        //display UI in JavaFX primary Stage
        uiManager.display(ui, primaryStage, "Hello Guarana!");
    }

    public static void main(String[] args) {

        // instantiate our main business object
        item = new ItemBean();
        item.setName("root");

        // handoff to JavaFX; this will call the start() method
        launch(args);
    }
}

