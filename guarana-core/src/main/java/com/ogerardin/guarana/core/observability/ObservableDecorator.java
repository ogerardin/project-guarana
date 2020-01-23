package com.ogerardin.guarana.core.observability;

import com.ogerardin.guarana.core.introspection.JavaIntrospector;
import com.ogerardin.guarana.core.metamodel.ClassInformation;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;

@Slf4j
public enum ObservableDecorator {
    ;

    public static <T> T of(T object, Class<T> objectClass) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(objectClass);
        ClassInformation<T> classInformation = JavaIntrospector.getClassInformation(objectClass);
        enhancer.setCallback(new SetterInterceptor<>(object, classInformation));
        //noinspection unchecked
        return (T) enhancer.create();
    }
}
