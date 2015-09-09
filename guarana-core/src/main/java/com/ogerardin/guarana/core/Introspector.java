package com.ogerardin.guarana.core;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

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
}
