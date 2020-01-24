package com.ogerardin.guarana.core.observability;

import com.ogerardin.guarana.core.introspection.JavaIntrospector;
import com.ogerardin.guarana.core.metamodel.ClassInformation;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;

@Slf4j
public enum ObservableFactory {
    ;

    public static <T> T createObservable(T object, Class<T> objectClass) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(objectClass);
        enhancer.setInterfaces(new Class[] {Observable.class});
        ClassInformation<T> classInformation = JavaIntrospector.getClassInformation(objectClass);
        enhancer.setCallback(new PropertyChangeInterceptor<>(object, classInformation));
        //noinspection unchecked
        return (T) enhancer.create();
    }
}
