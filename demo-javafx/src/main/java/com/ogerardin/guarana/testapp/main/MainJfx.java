package com.ogerardin.guarana.testapp.main;

/**
 * Created by Olivier on 28/05/15.
 */

import com.ogerardin.guarana.javafx.ui.InstanceUI;
import com.ogerardin.guarana.javafx.ui.ListUI;
import com.ogerardin.guarana.testapp.model.Person;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;

public class MainJfx extends Application {

    @Override
    public void start(Stage primaryStage) {
        VBox vBox = new VBox();

        {
            Button btn = new Button();
            btn.setText("Show Person");
            btn.setOnAction(event -> {
                final Person person = new Person("Olivier", "Gérardin");
                InstanceUI stage = new InstanceUI(Person.class);
                stage.setTarget(person);
                stage.setOnCloseRequest(e -> System.out.println(person));
                stage.show();
            });
            vBox.getChildren().add(btn);
        }

        {
            Button btn = new Button();
            btn.setText("Show List");
            btn.setOnAction(event -> {
                List<Person> persons = Arrays.asList(
                        new Person("Olivier", "Gérardin"),
                        new Person("Marcel", "Marceau")
                );
                ListUI stage = new ListUI(Person.class);
                stage.setTarget(persons);
                stage.show();
            });
            vBox.getChildren().add(btn);
        }

        Scene scene = new Scene(vBox, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);

        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}

