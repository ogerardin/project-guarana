/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.demo.javafx.domain2;

/**
 * @author Olivier
 * @since 28/05/15
 */

import com.ogerardin.guarana.domain2.model.Category;
import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.application.Application;
import javafx.stage.Stage;

public class DemoJfx2 extends Application {

    private static Category category;

    @Override
    public void start(Stage primaryStage) {

        // instantiate UiManager for JavaFX
        JfxUiManager uiManager = new JfxUiManager();

        // build UI for a Category and bind it to actual instance
        JfxInstanceUI<Category> ui = uiManager.buildInstanceUI(Category.class);
        ui.bind(category);

        //display UI in JavaFX primary Stage
        uiManager.display(ui, primaryStage, "Hello Guarana!");
    }

    public static void main(String[] args) {

        // instantiate our main business object
        category = new Category();
        category.setName("ROOT");

        // handoff to JavaFX; this will call the start() method
        launch(args);
    }
}

