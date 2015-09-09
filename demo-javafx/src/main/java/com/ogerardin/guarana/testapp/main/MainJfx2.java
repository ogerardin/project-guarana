package com.ogerardin.guarana.testapp.main;

/**
 * Created by Olivier on 28/05/15.
 */

import com.ogerardin.guarana.core.ui.InstanceUI;
import com.ogerardin.guarana.javafx.ui.builder.JfxUiBuilder;
import com.ogerardin.guarana.testapp.adapters.PersonManagerDb4oImpl;
import com.ogerardin.guarana.testapp.model.PersonManager;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainJfx2 extends Application {

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Hello World!");

        final JfxUiBuilder uiBuilder = JfxUiBuilder.INSTANCE;

        final InstanceUI<Parent, PersonManager> personManagerInstanceUI = uiBuilder.buildInstanceUI(PersonManager.class);
        personManagerInstanceUI.setTarget(new PersonManagerDb4oImpl());

        Scene scene = new Scene(personManagerInstanceUI.render());
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}

