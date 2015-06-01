package com.ogerardin.guarana.javafx.inspector;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import jfxtras.labs.scene.control.BeanPathAdapter;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Olivier on 29/05/15.
 */
public class InstanceInspector<T> extends Stage {

    private T target;

    private Map<PropertyDescriptor, Control> propertyDescriptorControlMap = new HashMap<PropertyDescriptor, Control>();
    private Map<Control, PropertyDescriptor> controlPropertyDescriptorMap = new HashMap<Control, PropertyDescriptor>();

    public InstanceInspector(Class<T> clazz) throws IntrospectionException {

        BeanInfo beanInfo = Introspector.getBeanInfo(clazz);

        this.setTitle(clazz.getName());
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Scene scene = new Scene(grid, 300, 275);
        this.setScene(scene);

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

                if (! isReadOnly(propertyDescriptor)) {
                    BeanPathAdapter<T> beanPathAdapter = new BeanPathAdapter<T>(target);
                    String propertyName = propertyDescriptor.getName();
                    beanPathAdapter.bindBidirectional(propertyName, textField.textProperty());
                }

            }

        }
    }
}
