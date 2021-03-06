/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui.impl;

import com.ogerardin.guarana.core.config.Util;
import com.ogerardin.guarana.core.introspection.JavaIntrospector;
import com.ogerardin.guarana.core.metamodel.ClassInformation;
import com.ogerardin.guarana.core.metamodel.PropertyInformation;
import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.ui.JfxCollectionUI;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Default implementation of {@link com.ogerardin.guarana.core.ui.CollectionUI} for JavaFX.
 *
 * @param <T> common type of collection items
 *
 * @author Olivier
 * @since 29/05/15
 */
@Slf4j
public class DefaultJfxCollectionUI<T> extends JfxUI implements JfxCollectionUI<T>, ListChangeListener<T> {

    private final ClassInformation<T> classInformation;

    SimpleListProperty<T> boundListProperty = new SimpleListProperty<T>();

    private final VBox root;
    private final TableView<T> tableView;

    Class<T> getItemClass() {
        return classInformation.getJavaClass();
    }

    public DefaultJfxCollectionUI(JfxUiManager builder, Class<T> itemClass) {
        super(builder);

        this.classInformation = JavaIntrospector.getClassInformation(itemClass);

        // title, icon
//        Image icon = classInformation.getIcon(BeanInfo.ICON_COLOR_32x32);
//        setTitle(itemClass.getSimpleName());
//        if (icon != null) {
//            try {
//                this.getIcons().add(ImageUtil.createImage(icon));
//            } catch (IOException ignored) {
//            }
//        }

        root = new VBox();
        Label titleLabel;
        {
            BorderPane titleBox = new BorderPane();
            final String title = getConfiguration().getClassDisplayName(itemClass);
            titleLabel = new Label(title);
            titleLabel.setFont(getTitleLabelFont());
            titleBox.setCenter(titleLabel);
            {
                HBox buttonBox = new HBox();
                {
                    Button button = new Button("-");
                    button.setOnAction(event -> removeItem());
                    buttonBox.getChildren().add(button);
                }
                {
                    Button button = new Button("+");
                    button.setOnAction(event -> addNewItem());
                    buttonBox.getChildren().add(button);
                }
                titleBox.setLeft(buttonBox);
            }
            {
                Button button = new Button("↻");
                button.setOnAction(event -> refresh());
                titleBox.setRight(button);
            }

            root.getChildren().add(titleBox);
        }


        // build table
        tableView = new TableView<>();
        tableView.setEditable(false);
        for (PropertyInformation propertyInformation : classInformation.getProperties()) {
            final String propertyName = propertyInformation.getName();
            String displayName = propertyInformation.getDisplayName();
            if (propertyName.equals(displayName) && getConfiguration().isHumanizePropertyNames(itemClass)) {
                displayName = Util.humanize(propertyName);
            }
            TableColumn<T, ?> column = new TableColumn<>(displayName);
            column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
            column.setVisible(builder.getConfiguration().isShownProperty(itemClass, propertyName));
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
            configureContextMenu(row, classInformation, row::getItem);

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

        configureDragSource(titleLabel, tableView::getItems);
        configureContextMenu(titleLabel, classInformation, null);

    }

    private void refresh() {
        tableView.refresh();
    }

    private void removeItem() {
        //TODO
    }

    private void handleDoubleClick(TableRow<T> row) {
        if (!row.isEmpty()) {
            editItem(row);
        }
    }

    private void editItem(TableRow<T> row) {
        T item = row.getItem();
        Class<T> itemClass = getItemClass();
        final int rowIndex = row.getIndex();

        String itemTitle = "Item " + (rowIndex + 1);
        JfxInstanceUI<T> ui = getBuilder().buildInstanceUI(itemClass);
        ui.display(item);
        ui.boundObjectProperty().addListener((observable, oldValue, newValue) -> {
            log.debug("List item changed: " + oldValue + " -> " + newValue);
        });

        final Dialog<T> dialog = getDialog(item, ui);
        dialog.showAndWait()
                .ifPresent(i -> {
                    ui.populate(i);
                    log.debug("Form submitted, updating item {}", i);
                    //make sure changes on item are reflected in the list UI
                    tableView.refresh();
                });

    }

    private void addNewItem() {
        Class<T> itemClass = getItemClass();
        T item;
        try {
            item = itemClass.newInstance();
        } catch (Exception e) {
            // no-arg constructor does not exist, is not public, or failed
            //TODO better message
            getBuilder().displayException(e);
            return;
        }

        //substitute item with observable proxy
//        item = ObservableFactory.createObservable(item, itemClass);
//        ((Observable) item).addPropertyChangeListener(propertyChangeEvent -> {
//            log.debug("PropertyChangeListener notified!");
//        });

//        JfxInstanceUI<T> ui = getBuilder().displayInstance(item, itemClass, "New Item");
        JfxInstanceUI<T> ui = getBuilder().buildInstanceUI(itemClass);
        ui.display(item);
        ui.boundObjectProperty().addListener((observable, oldValue, newValue) -> {
            log.debug("List item changed: " + oldValue + " -> " + newValue);
        });
        Dialog<T> dialog = getDialog(item, ui);

        dialog.showAndWait()
                .ifPresent(i -> {
                    ui.populate(item);
                    log.debug("Form submitted, adding item {}", item);
                    tableView.getItems().add(i);
                });
    }

    private Dialog<T> getDialog(T item, JfxInstanceUI<T> ui) {
        // using native JavaFX Dialog
        //TODO move this to JfxUiManager
        Dialog<T> dialog = new Dialog<>();
        dialog.initModality(Modality.NONE);
        dialog.getDialogPane().setContent(ui.getRendered());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
        T finalItem = item;
        dialog.setResultConverter(buttonType -> finalItem);
        return dialog;
    }


    @Override
    public Parent getRendered() {
        return root;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        tableView.setEditable(!readOnly);
    }

    @Override
    public void bind(Collection<? extends T> collection) {
        log.debug("Binding collection " + collection.getClass() + " to " + this);
        // stop listening to existing ObservableList if any
        if (getBoundList() != null) {
            getBoundList().removeListener(this);
        }

        // get ObservableList from argument
        ObservableList<T> observableList;
        if (collection instanceof ObservableList) {
            observableList = (ObservableList<T>) collection;
            log.debug("Collection bound as native ObservableList");
        } else if (collection instanceof List) {
            observableList = FXCollections.observableList((List<T>) collection);
            log.debug("Collection wrapped into ObservableList using FXCollections.observableList");
        } else {
            ArrayList<T> list = new ArrayList<>(collection);
            observableList = FXCollections.observableList(list);
            log.warn("Collection copied to ObservableList");
        }

        // start listening and set view
        boundListProperty.set(observableList);
        observableList.addListener(this);
        tableView.setItems(observableList);
    }

    private ObservableList<T> getBoundList() {
        return boundListProperty.get();
    }

    @Override
    public void onChanged(Change<? extends T> change) {
        log.debug("list changed: " + change);
    }
}
