/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui.impl;

import com.ogerardin.guarana.core.introspection.Introspector;
import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.ui.JfxCollectionUI;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Olivier
 * @since 29/05/15
 */
public class DefaultJfxCollectionUI<T> extends JfxUI implements JfxCollectionUI<T> {

    private final Class<T> itemClass;
    private final BeanInfo beanInfo;

    private final VBox root;
    private final TableView<T> tableView;

    public DefaultJfxCollectionUI(JfxUiManager builder, Class<T> itemClass) {
        super(builder);

        this.itemClass = itemClass;
        this.beanInfo = Introspector.getClassInfo(itemClass);

        // title, icon
//        Image icon = beanInfo.getIcon(BeanInfo.ICON_COLOR_32x32);
//        setTitle(itemClass.getSimpleName());
//        if (icon != null) {
//            try {
//                this.getIcons().add(ImageUtil.createImage(icon));
//            } catch (IOException ignored) {
//            }
//        }

        root = new VBox();
        final Label title = new Label(beanInfo.getBeanDescriptor().getDisplayName() + "...");
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        root.getChildren().add(title);

        // build table
        tableView = new TableView<>();
        tableView.setEditable(false);
        for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
            final String propertyName = propertyDescriptor.getName();
            String displayName = propertyDescriptor.getDisplayName();
            if (propertyName.equals(displayName) && getConfiguration().getHumanizePropertyNames()) {
                displayName = Introspector.humanize(propertyName);
            }
            TableColumn<T, ?> column = new TableColumn<>(displayName);
            column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
            column.setVisible(!builder.getConfiguration().forClass(itemClass).isHiddenProperty(propertyName));
            tableView.getColumns().add(column);
        }

        tableView.setRowFactory(tv -> {
            TableRow<T> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                // Handle double-click
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    handleDoubleClick(row);
                }
            });

            //Set context menu for row
            configureContextMenu(row, beanInfo, row::getItem);

            //Set row as drag-and-drop source
            configureDragSource(row, row::getItem);

            return row;
        });

        tableView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                ObservableList<T> selectedItems = tableView.getSelectionModel().getSelectedItems();
                tableView.getItems().removeAll(selectedItems);
            }
        });

        configureDropTarget(tableView,
                value -> itemClass.isAssignableFrom(value.getClass()),
                value -> tableView.getItems().add((T) value)
        );

        root.getChildren().add(tableView);

    }

    private void handleDoubleClick(TableRow<T> row) {
        String itemTitle;
        T item = null;
        if (!row.isEmpty()) {
            itemTitle = "Item " + (row.getIndex() + 1);
            item = row.getItem();
        } else {
            itemTitle = "New Item";
            try {
                // try no-arg constructor.
                item = itemClass.newInstance();
                tableView.getItems().add(item);
            } catch (Exception e) {
                //TODO better message
                getBuilder().displayException(e);
            }
        }
        if (item != null) {
            getBuilder().displayInstance(item, itemClass, row, itemTitle);
        }
    }


    @Override
    public Parent render() {
        return root;
    }

    @Override
    public void setTarget(Collection<? extends T> target) {
        if (target instanceof ObservableList) {
            tableView.setItems((ObservableList<T>) target);
        } else if (target instanceof List) {
            tableView.setItems(FXCollections.observableList((List<T>) target));
        } else {
            tableView.setItems(FXCollections.observableList(new ArrayList<T>(target)));
        }
    }
}