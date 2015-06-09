package com.ogerardin.guarana.javafx.ui;

import com.ogerardin.guarana.javafx.util.ImageUtil;
import javafx.scene.control.Control;
import javafx.stage.Stage;

import java.awt.*;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Olivier on 09/06/15.
 */
public class GuaranaUI<T> extends Stage {

    protected Map<PropertyDescriptor, Control> propertyDescriptorControlMap = new HashMap<PropertyDescriptor, Control>();
    protected Map<Control, PropertyDescriptor> controlPropertyDescriptorMap = new HashMap<Control, PropertyDescriptor>();

    protected final BeanInfo beanInfo;

    public <T> GuaranaUI(Class<T> clazz) {
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }

        Image icon = beanInfo.getIcon(BeanInfo.ICON_COLOR_32x32);

        // title, icon
        this.setTitle(clazz.getSimpleName());
        if (icon != null) {
            try {
                this.getIcons().add(ImageUtil.createImage(icon));
            } catch (IOException ignored) {
            }
        }

    }

    public static boolean isReadOnly(PropertyDescriptor propertyDescriptor) {
        return propertyDescriptor.getWriteMethod() == null;
    }

}
