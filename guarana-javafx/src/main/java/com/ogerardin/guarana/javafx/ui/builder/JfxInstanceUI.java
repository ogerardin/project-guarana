package com.ogerardin.guarana.javafx.ui.builder;

import com.ogerardin.guarana.core.Introspector;
import com.ogerardin.guarana.core.ui.InstanceUI;
import com.ogerardin.guarana.javafx.ui.Defaults;
import com.ogerardin.guarana.javafx.util.DialogUtil;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import jfxtras.labs.scene.control.BeanPathAdapter;

import java.awt.*;
import java.beans.BeanInfo;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
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

        Image icon = beanInfo.getIcon(BeanInfo.ICON_COLOR_32x32);

        // title, icon
//        setTitle(clazz.getSimpleName());
//        if (icon != null) {
//            try {
//                this.getIcons().add(ImageUtil.createImage(icon));
//            } catch (IOException ignored) {
//            }
//        }

        root = new VBox();

        root.getChildren().add(new Label(beanInfo.getBeanDescriptor().getDisplayName()));

        // methods
        {
            ContextMenu contextMenu = new ContextMenu();
            Arrays.asList(beanInfo.getMethodDescriptors()).stream()
                    .map(md -> new MenuItem(md.getDisplayName()) {
                        {
                            setOnAction(event -> executeMethodRequested(md));
                        }
                    })
                    .forEach(menuItem -> contextMenu.getItems().add(menuItem));

//            root.setContextMenu(contextMenu);
            root.setOnMouseClicked(event -> contextMenu.show(root, Side.BOTTOM, 0, 0));
        }

        // properties
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(Defaults.DEFAULT_INSETS);

        root.getChildren().add(grid);
        int row = 0;
        for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
            Label label = new Label(propertyDescriptor.getDisplayName());
            grid.add(label, 0, row);

            TextField field = new TextField();
            grid.add(field, 1, row);
            //FIXME: for now only editable String properties generate an editable text field
            if (isReadOnly(propertyDescriptor) || propertyDescriptor.getPropertyType() != String.class) {
                field.setEditable(false);
            }
            if (row == 0) {
                field.requestFocus();
            }

            propertyDescriptorControlMap.put(propertyDescriptor, field);
            controlPropertyDescriptorMap.put(field, propertyDescriptor);

            row++;
        }

    }

    private void executeMethodRequested(MethodDescriptor md) {
        System.out.print(md.getName());
        Method method = md.getMethod();

        final Class<?> returnType = method.getReturnType();

        Object result;
        // if no arg, execute immediately, otherwise display arg dialog
        if (method.getParameterCount() == 0) {
            try {
                result = method.invoke(target);
                throw new RuntimeException("bla");
            } catch (Exception e) {
                DialogUtil.showExceptionDialog(e);
            }
        } else {
            //TODO
        }
    }

    public static boolean isReadOnly(PropertyDescriptor propertyDescriptor) {
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
