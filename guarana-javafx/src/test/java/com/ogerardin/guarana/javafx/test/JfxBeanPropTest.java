/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.test;import javafx.beans.property.Property;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

/**
 * @author oge
 * @since 06/03/2017
 */
public class JfxBeanPropTest {

    @Test
    public void testJfxBeanProp() throws NoSuchMethodException {
        Container container = new Container();
        container.setName("BLA");

        Property<String> jfxProperty = JavaBeanObjectPropertyBuilder.create()
                .bean(container)
                .name("name")
                .build();

//        jfxProperty.addListener((observable, oldValue, newValue) -> {
//            System.out.println("jfx property changed: " + oldValue + " --> " + newValue);
//        });


        for (int i=0; i<10000; i++) {
            final String value = UUID.randomUUID().toString();
            jfxProperty.setValue(value);
            Assert.assertEquals(value, container.getName());
        }


    }
}
