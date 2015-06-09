package com.ogerardin.guarana.javafx.ui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Created by Olivier on 05/06/15.
 */
public class MethodCallUI extends Stage {

    public MethodCallUI(Method method) {

        this.setTitle(method.getName());

        VBox vBox = new VBox();
        Scene scene = new Scene(vBox);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(Defaults.DEFAULT_INSETS);

        vBox.getChildren().add(grid);

        int row = 0;
        for (Parameter param : method.getParameters()) {

            String name = param.getName();
            Label label = new Label(name);
            grid.add(label, 0, row);

            TextField field = new TextField();
            grid.add(field, 1, row);
            if (row == 0) {
                field.requestFocus();
            }
            row++;
        }

        Button button = new Button("Go");
        button.setOnAction(event -> System.out.println("Go"));

        vBox.getChildren().add(button);

        this.setScene(scene);
    }
}
