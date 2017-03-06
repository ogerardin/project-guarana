/*
 * Copyright (c) 2017 Olivier Gérardin
 */

import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.application.Application;
import javafx.stage.Stage;

public class DummyTestJfx extends Application {

    private static Container container;

    @Override
    public void start(Stage primaryStage) {

        // instantiate UiManager for JavaFX
        JfxUiManager uiManager = new JfxUiManager();

        // build UI for a DomainManager and bind it to actual instance
        JfxInstanceUI<Container> ui = uiManager.buildInstanceUI(Container.class);
        ui.bind(container);

        //display UI in JavaFX primary Stage
        uiManager.display(ui, primaryStage, "Hello Guarana!");
    }

    public static void main(String[] args) {

        // instantiate our main business object
        container = new Container();
        container.setName("root");

        // handoff to JavaFX; this will call the start() method
        launch(args);
    }
}

