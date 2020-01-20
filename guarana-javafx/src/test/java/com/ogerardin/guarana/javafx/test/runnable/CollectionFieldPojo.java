/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.test.runnable;

import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.test.items.ItemPojo;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.application.Application;
import javafx.stage.Stage;
import lombok.Data;

import java.util.List;

public class CollectionFieldPojo extends Application {

    @Data
    public static class MyClass {
        public List<ItemPojo> items;
    }

    @Override
    public void start(Stage primaryStage) {

        // instantiate UiManager for JavaFX
        JfxUiManager uiManager = new JfxUiManager();

        // build UI and bind it to actual instance
        JfxInstanceUI<MyClass> ui = uiManager.buildInstanceUI(MyClass.class);
        ui.bind(new MyClass());

        //display UI in JavaFX primary Stage
        uiManager.display(ui, primaryStage, "Hello Guarana!");
    }

    public static void main(String[] args) {
        // handoff to JavaFX; this will call the start() method
        launch(args);
    }
}

