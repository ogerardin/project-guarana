package com.ogerardin.guarana.javafx.ui.builder;

import com.ogerardin.guarana.core.Introspector;
import com.ogerardin.guarana.core.ui.CollectionUI;
import com.ogerardin.guarana.core.ui.InstanceUI;
import com.ogerardin.guarana.javafx.util.DialogUtil;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.List;

/**
 * Created by Olivier on 29/05/15.
 */
public class JfxCollectionUI<T> implements CollectionUI<Parent, T> {

    protected final BeanInfo beanInfo;
    private final Class<T> itemClass;
    private Collection<T> target;

    private final VBox root;
    private final TableView<T> tableView;

    public JfxCollectionUI(Class<T> itemClass) {

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
        final Label title = new Label(beanInfo.getBeanDescriptor().getDisplayName() + "*");
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        root.getChildren().add(title);

        // build table
        tableView = new TableView<>();
        tableView.setEditable(false);
        for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
            final String propertyName = propertyDescriptor.getName();
            TableColumn column = new TableColumn(propertyDescriptor.getDisplayName());
            column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
            if (propertyName.equals("class")) {
                column.setVisible(false);
            }
            tableView.getColumns().add(column);
        }

        tableView.setRowFactory(tv -> {
            TableRow<T> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && (!row.isEmpty())) {
                    T item = row.getItem();
                    InstanceUI<Parent, T> instanceUI = JfxUiBuilder.INSTANCE.buildInstanceUI(itemClass);
                    instanceUI.setTarget(item);
                    DialogUtil.display(instanceUI, "Collection");
                }
            });
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
