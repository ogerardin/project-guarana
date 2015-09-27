package com.ogerardin.guarana.javafx.ui;

import com.ogerardin.guarana.core.introspection.Introspector;
import com.ogerardin.guarana.core.ui.CollectionUI;
import com.ogerardin.guarana.core.ui.InstanceUI;
import com.ogerardin.guarana.javafx.JfxUiBuilder;
import com.ogerardin.guarana.javafx.util.DialogUtil;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import jfxtras.labs.scene.control.BeanPathAdapter;

import java.beans.BeanInfo;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Olivier on 29/05/15.
 */
public class JfxInstanceUI<T> implements InstanceUI<Parent, T> {

    protected final BeanInfo beanInfo;

    protected Map<PropertyDescriptor, Control> propertyDescriptorControlMap = new HashMap<PropertyDescriptor, Control>();
    protected Map<Control, PropertyDescriptor> controlPropertyDescriptorMap = new HashMap<Control, PropertyDescriptor>();

    private final VBox root;

    private T target;

    public JfxInstanceUI(Class<T> clazz) {
        beanInfo = Introspector.getClassInfo(clazz);

        root = new VBox();
        final Label title = new Label(beanInfo.getBeanDescriptor().getDisplayName());
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        root.getChildren().add(title);

        // build methods context menu
        {
            ContextMenu contextMenu = getContextMenu(beanInfo);
            title.setContextMenu(contextMenu);
            //root.setOnMouseClicked(event -> contextMenu.show(root, Side.BOTTOM, 0, 0));
        }

        // build properties form
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(Defaults.DEFAULT_INSETS);

        ColumnConstraints column2 = new ColumnConstraints();
        column2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().setAll(new ColumnConstraints(), column2); // second column gets any extra width

        root.getChildren().add(grid);
        int row = 0;
        for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
            // ignore "class" property
            if (propertyDescriptor.getName().equals("class")) {
                continue;
            }

            final String humanizedName = Introspector.humanize(propertyDescriptor.getDisplayName());
            Label label = new Label(humanizedName);
            grid.add(label, 0, row);

            TextField field = new TextField();
            grid.add(field, 1, row);
            //FIXME: for now only editable String properties generate an editable text field
            if (Introspector.isReadOnly(propertyDescriptor) || propertyDescriptor.getPropertyType() != String.class) {
                field.setEditable(false);
            }
            if (row == 0) {
                field.requestFocus();
            }

            // if it's a collection, add a button to open as list
            if (Collection.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
                Button button = new Button("...");
                button.setOnAction(e -> {
                    System.out.println(propertyDescriptor.getName());
                    final Method readMethod = propertyDescriptor.getReadMethod();
                    try {
                        final Collection collection = (Collection) readMethod.invoke(target);
                        Class itemClass = Object.class;
                        //FIXME how do we get the item class if collection is empty ??
                        if (!collection.isEmpty()) {
                            itemClass = collection.iterator().next().getClass();
                        }
                        CollectionUI<Parent, ?> collectionUI = JfxUiBuilder.INSTANCE.buildCollectionUi(itemClass);
                        collectionUI.setTarget(collection);
                        DialogUtil.display(collectionUI, humanizedName);

                    } catch (Exception ex) {
                        DialogUtil.displayException(ex);
                    }
                });
                grid.add(button, 2, row);
            }

            propertyDescriptorControlMap.put(propertyDescriptor, field);
            controlPropertyDescriptorMap.put(field, propertyDescriptor);

            row++;
        }

    }

    private ContextMenu getContextMenu(BeanInfo beanInfo) {
        ContextMenu contextMenu = new ContextMenu();
        Arrays.asList(beanInfo.getMethodDescriptors()).stream()
                .filter(md -> !Introspector.isGetterOrSetter(md))
                .map(md -> new MenuItem(md.getDisplayName()) {
                    {
                        setOnAction(event -> executeMethodRequested(md));
                    }
                })
                .forEach(menuItem -> contextMenu.getItems().add(menuItem));
        Arrays.asList(beanInfo.getBeanDescriptor().getBeanClass().getDeclaredConstructors()).stream()
                .map(constructor -> new MenuItem(constructor.toGenericString()) {
                    {
                        setOnAction(event -> executeConstructorRequested(constructor));
                    }
                })
                .forEach(menuItem -> contextMenu.getItems().add(menuItem));
        return contextMenu;
    }

    private <T> void executeConstructorRequested(Constructor<T> constructor) {
        System.out.println(constructor.toGenericString());

        T instance;
        if (constructor.getParameterCount() ==0) {
            try {
                instance = constructor.newInstance();
                DialogUtil.displayInstance((Class<T>) beanInfo.getClass(), instance, "New Instance");
            } catch (Exception e) {
                e.printStackTrace();
                DialogUtil.displayException(e);
            }
        }
        else {
            JfxMethodCallUI methodCallUI = new JfxMethodCallUI(constructor);
            DialogUtil.display(methodCallUI);
        }
    }

    private void executeMethodRequested(MethodDescriptor md) {
        System.out.println(md.getName());
        Method method = md.getMethod();

        final Class returnType = (Class) method.getReturnType();

        Object result;
        // if no arg, execute immediately, otherwise display arg dialog
        if (method.getParameterCount() == 0) {
            try {
                result = method.invoke(target);
                DialogUtil.displayInstance(returnType, result, "Result");
            } catch (Exception e) {
                e.printStackTrace();
                DialogUtil.displayException(e);
            }
        } else {
            JfxMethodCallUI methodCallUI = new JfxMethodCallUI(method);
            DialogUtil.display(methodCallUI);
        }
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

                if (textField.isEditable()) {
                    BeanPathAdapter<T> beanPathAdapter = new BeanPathAdapter<T>(target);
                    String propertyName = propertyDescriptor.getName();
                    beanPathAdapter.bindBidirectional(propertyName, textField.textProperty());
                } else {
                    //FIXME we should bind (unidirectionally) and not just set property value
                    try {
                        textField.setText(propertyDescriptor.getReadMethod().invoke(target).toString());
                    } catch (Exception ignored) {
                    }
                }


            }

        }
    }

    @Override
    public Parent render() {
        return root;
    }
}
