/*
 * Copyright (c) 2016 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.binding;

import com.ogerardin.guarana.javafx.JfxUiManager;
import javafx.util.StringConverter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author oge
 * @since 17/06/2016
 */
public class TargetStringConverter<X> extends StringConverter<X> {

    private final JfxUiManager jfxUiManager;
    private final Class<X> clazz;

    public TargetStringConverter(JfxUiManager jfxUiManager, Class<X> clazz) {
        this.jfxUiManager = jfxUiManager;
        this.clazz = clazz;
    }

    @Override
    public String toString(X object) {
        return jfxUiManager.getConfiguration().forClass(clazz).toString(object);
    }

    @Override
    public X fromString(String string) {
        try {
            final Constructor<X> constructor = clazz.getConstructor(String.class);
            final X object = constructor.newInstance(string);
            return object;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Can't convert from String to " + clazz + ": no contructor "
                    + clazz.getName() + "(String) found");
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Contructor invocation " + clazz.getName() + "(\"" + string + "\") failed", e);
        }
    }
}
