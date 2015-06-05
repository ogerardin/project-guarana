package com.ogerardin.guarana.javafx.inspector;

import com.ogerardin.guarana.javafx.util.Util;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jfxtras.labs.scene.control.BeanPathAdapter;

import java.awt.Image;
import java.beans.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Olivier on 29/05/15.
 */
public class InstanceInspector<T> extends Stage {

    public static final Insets DEFAULT_INSETS = new Insets(25, 25, 25, 25);

    private T target;

    private Map<PropertyDescriptor, Control> propertyDescriptorControlMap = new HashMap<PropertyDescriptor, Control>();
    private Map<Control, PropertyDescriptor> controlPropertyDescriptorMap = new HashMap<Control, PropertyDescriptor>();

    public InstanceInspector(Class<T> clazz) throws IntrospectionException {

        BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
        Image icon = beanInfo.getIcon(BeanInfo.ICON_COLOR_32x32);

        // title, icon
        this.setTitle(clazz.getSimpleName());
        if (icon != null) {
            try {
                this.getIcons().add(Util.createImage(icon));
            } catch (IOException ignored) {
            }
        }

        VBox vBox = new VBox();
        Scene scene = new Scene(vBox);

        // methods
        {
            ComboBox<String> comboBox = new ComboBox<>();
            //comboBox.setPadding(DEFAULT_INSETS);
            vBox.getChildren().add(comboBox);
            for (MethodDescriptor methodDescriptor : beanInfo.getMethodDescriptors()) {
                Method method = methodDescriptor.getMethod();
                if (! Modifier.isStatic(method.getModifiers())) {
                    comboBox.getItems().add(methodDescriptor.getDisplayName());
                }
            }
            comboBox.setOnAction(event -> {
                System.out.println(comboBox.getValue());

            });
        }

        // properties
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(DEFAULT_INSETS);

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

            propertyDescriptorControlMap.put(propertyDescriptor, field);
            controlPropertyDescriptorMap.put(field, propertyDescriptor);

            row++;
        }

        this.setScene(scene);

    }

    private static boolean isReadOnly(PropertyDescriptor propertyDescriptor) {
        return propertyDescriptor.getWriteMethod() == null;
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
