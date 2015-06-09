package com.ogerardin.guarana.javafx.ui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import jfxtras.labs.scene.control.BeanPathAdapter;

import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * Created by Olivier on 29/05/15.
 */
public class InstanceUI<T> extends GuaranaUI<T> {

    private T target;

    public InstanceUI(Class<T> clazz) {

        super(clazz);

        VBox vBox = new VBox();
        Scene scene = new Scene(vBox);

        // methods
        {
            ComboBox<MethodDescriptor> comboBox = new ComboBox<>();
            comboBox.setConverter(new StringConverter<MethodDescriptor>() {
                public String toString(MethodDescriptor methodDescriptor) {
                    return methodDescriptor.getDisplayName();
                }
                public MethodDescriptor fromString(String string) {
                    return null;
                }
            });
            //comboBox.setPadding(DEFAULT_INSETS);
            vBox.getChildren().add(comboBox);
            for (MethodDescriptor methodDescriptor : beanInfo.getMethodDescriptors()) {
                Method method = methodDescriptor.getMethod();
                if (! Modifier.isStatic(method.getModifiers())) {
                    comboBox.getItems().add(methodDescriptor);
                }
            }
            comboBox.setOnAction(event -> {
                MethodDescriptor methodDescriptor = comboBox.getValue();
                MethodCallUI methodCallUI = new MethodCallUI(methodDescriptor.getMethod());
                methodCallUI.show();
            });
        }

        // properties
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(Defaults.DEFAULT_INSETS);

        vBox.getChildren().add(grid);
        int row = 0;
        for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
            Label label = new Label(propertyDescriptor.getDisplayName());
            grid.add(label, 0, row);

            TextField field = new TextField();
            grid.add(field, 1, row);
            if (isReadOnly(propertyDescriptor)) {
                field.setEditable(false);
            }
            if (row == 0) {
                field.requestFocus();
            }

            propertyDescriptorControlMap.put(propertyDescriptor, field);
            controlPropertyDescriptorMap.put(field, propertyDescriptor);

            row++;
        }

        this.setScene(scene);

    }



    public void setTarget(T target) {
        if (this.target != null) {
            unbind(this.target);
        }
        bind(target);
        this.target = target;
    }

    private void unbind(T target) {
        //TODO
    }

    private void bind(T target) {
        for (Map.Entry<Control, PropertyDescriptor> entry : controlPropertyDescriptorMap.entrySet()) {
            Control control = entry.getKey();
            PropertyDescriptor propertyDescriptor = entry.getValue();

            if (control instanceof TextField) {
                TextField textField = (TextField) control;

                if (!isReadOnly(propertyDescriptor)) {
                    BeanPathAdapter<T> beanPathAdapter = new BeanPathAdapter<T>(target);
                    String propertyName = propertyDescriptor.getName();
                    beanPathAdapter.bindBidirectional(propertyName, textField.textProperty());
                }

            }

        }
    }
}
