package com.ogerardin.guarana.testapp.main;

/**
 * Created by Olivier on 28/05/15.
 */

import com.ogerardin.guarana.javafx.inspector.InstanceInspector;
import com.ogerardin.guarana.testapp.model.Person;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.beans.IntrospectionException;

public class MainJfx extends Application {

    @Override
    public void start(Stage primaryStage) {
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                final Person person = new Person("Olivier", "Gérardin");

                try {
                    InstanceInspector stage = new InstanceInspector(Person.class);
                    stage.setTarget(person);

                    stage.setOnCloseRequest(e -> System.out.println(person));

                    stage.show();
                } catch (IntrospectionException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(btn);

        Scene scene = new Scene(root, 300, 250);


        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);

        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}

