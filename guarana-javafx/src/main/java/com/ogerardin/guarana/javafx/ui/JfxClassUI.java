/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui;

import com.ogerardin.guarana.core.introspection.Introspector;
import com.ogerardin.guarana.core.ui.ClassUI;
import com.ogerardin.guarana.javafx.JfxUiManager;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;

import java.beans.BeanInfo;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * @author oge
 * @since 07/09/2015.
 */
@Deprecated
public class JfxClassUI extends Label implements ClassUI<Parent> {

    public JfxClassUI(JfxUiManager builder, Class clazz) {
        super(clazz.getName());

        ContextMenu contextMenu = new ContextMenu();

        BeanInfo beanInfo = Introspector.getClassInfo(clazz);

        Arrays.asList(beanInfo.getMethodDescriptors()).stream()                 // list class methods
                .filter(md -> Modifier.isStatic(md.getMethod().getModifiers())) // filter static only
                .map(md -> new MenuItem(md.getDisplayName()) {                  // map to MenuItem
                    {
                        setOnAction(event -> System.out.println(md.getName()));
                    }
                })
                .forEach(menuItem -> contextMenu.getItems().add(menuItem));     // add to context menu

        // use as contextual menu (right-lick)
        setContextMenu(contextMenu);

        // also display if left click
        setOnMouseClicked(event -> contextMenu.show(this, Side.BOTTOM, 0, 0));
    }

    @Override
    public Parent render() {
        return this;
    }
}
