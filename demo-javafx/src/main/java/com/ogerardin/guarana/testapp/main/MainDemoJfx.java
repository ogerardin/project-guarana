package com.ogerardin.guarana.testapp.main;

/**
 * Created by Olivier on 28/05/15.
 */

import com.ogerardin.guarana.core.ui.InstanceUI;
import com.ogerardin.guarana.core.ui.UIBuilder;
import com.ogerardin.guarana.javafx.ui.builder.JfxUiBuilder;
import com.ogerardin.guarana.javafx.util.DialogUtil;
import com.ogerardin.guarana.testapp.adapters.PersonManagerDb4oImpl;
import com.ogerardin.guarana.testapp.model.PersonManager;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class MainDemoJfx extends Application {

    @Override
    public void start(Stage primaryStage) {

        final PersonManager pm = new PersonManagerDb4oImpl();

        // 1) obtain instance of UiBuilder
        final UIBuilder uiBuilder = JfxUiBuilder.INSTANCE;

        // 2) build UI for PersonManager
        final InstanceUI<Parent, PersonManager> ui = uiBuilder.buildInstanceUI(PersonManager.class);

        // 3) bind UI to instance
        ui.setTarget(pm);

        // 4) display
        DialogUtil.display(primaryStage, ui, "Hello Guarana!");
    }

    public static void main(String[] args) {
        launch(args);
    }
}

