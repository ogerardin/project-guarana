package com.ogerardin.guarana.javafx.ui;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.beans.PropertyDescriptor;
import java.util.List;

/**
 * Created by Olivier on 29/05/15.
 */
public class ListUI<T> extends GuaranaUI<T> {

    private final TableView<T> table;

    public ListUI(Class<T> clazz) {

        super(clazz);

        VBox vBox = new VBox();
        Scene scene = new Scene(vBox);

        // properties
        table = new TableView<T>();
        vBox.getChildren().add(table);

        for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
            TableColumn<T, String> tc = new TableColumn<T, String>(propertyDescriptor.getDisplayName());
            tc.setMinWidth(120);
            tc.setCellValueFactory(new PropertyValueFactory<T, String>(propertyDescriptor.getName()));
            table.getColumns().add(tc);
        }

        this.setScene(scene);
    }

    public void setTarget(List<T> target) {
        table.setItems(new ObservableListWrapper<T>(target));
    }

}
