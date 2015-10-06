/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.introspection;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by oge on 07/09/2015.
 */
public class Introspector {

    private static Map<Class, BeanInfo> classInfoMap = new HashMap<Class, BeanInfo>();

    private Introspector() {
    }

    public static BeanInfo getClassInfo(Class clazz) {
        BeanInfo beanInfo = classInfoMap.get(clazz);
        if (beanInfo != null) {
            return beanInfo;
        }
        try {
            beanInfo = java.beans.Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
        classInfoMap.put(clazz, beanInfo);
        return beanInfo;
    }

    public static String humanize(String propertyName) {
        // split "camelCase" to "camel" "Case"
        final String[] parts = propertyName.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
        // fix case of each part and join into a space-separated string
        return Arrays.stream(parts)
                .map(part -> part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    public static boolean isGetterOrSetter(MethodDescriptor methodDescriptor) {
        String methodName = methodDescriptor.getName();
        return methodName.startsWith("get")
                || methodName.startsWith("is")
                || methodName.startsWith("set");
    }

    public static boolean isReadOnly(PropertyDescriptor propertyDescriptor) {
        return propertyDescriptor.getWriteMethod() == null;
    }
}
