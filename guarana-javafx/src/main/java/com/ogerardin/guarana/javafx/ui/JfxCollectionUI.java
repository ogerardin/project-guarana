/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui;

import com.ogerardin.guarana.core.introspection.Introspector;
import com.ogerardin.guarana.core.ui.CollectionUI;
import com.ogerardin.guarana.core.ui.InstanceUI;
import com.ogerardin.guarana.javafx.JfxUiBuilder;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.List;

/**
 * @author Olivier
 * @since 29/05/15
 */
public class JfxCollectionUI<T> extends JfxUI implements CollectionUI<Parent, T> {

    protected final BeanInfo beanInfo;
    private final Class<T> itemClass;
    private Collection<T> target;

    private final VBox root;
    private final TableView<T> tableView;

    public JfxCollectionUI(JfxUiBuilder builder, Class<T> itemClass) {
        super(builder);

        beanInfo = Introspector.getClassInfo(itemClass);
        this.itemClass = itemClass;

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
            TableColumn column = new TableColumn(displayName);

            column.setCellValueFactory(new PropertyValueFactory<>(propertyName));

            if (builder.getConfiguration().forClass(itemClass).isHiddenProperty(propertyName)) {
                column.setVisible(false);
            }
            tableView.getColumns().add(column);
        }

        tableView.setRowFactory(tv -> {
            TableRow<T> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                // Handle double-click
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    String itemTitle;
                    T item = null;
                    if (!row.isEmpty()) {
                        itemTitle = "Item " + row.getIndex();
                        item = row.getItem();
                    } else {
                        itemTitle = "New Item";
                        try {
                            // try no-arg constructor.
                            item = itemClass.newInstance();
                        } catch (Exception e) {
                            getBuilder().displayException(e);
                        }
                    }
                    if (item != null) {
                        InstanceUI<Parent, T> instanceUI = builder.buildInstanceUI(itemClass);
                        instanceUI.setTarget(item);
                        getBuilder().display(instanceUI, row, itemTitle);
                    }
                }
            });

            //Set context menu for row
            configureContextMenu(row, beanInfo, row::getItem);

            //Set row as drag-and-drop source
            configureDragDropSource(row, row::getItem);

            return row;
        });

        root.getChildren().add(tableView);

    }


    @Override
    public Parent render() {
        return root;
    }

    @Override
    public void setTarget(Collection<? extends T> target) {
        //FIXME only list supported currently
        tableView.setItems(FXCollections.observableList((List<T>)target));
        this.target = (Collection<T>) target;
    }
}
