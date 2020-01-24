package com.ogerardin.guarana.core.observability;

import com.ogerardin.guarana.core.introspection.JavaIntrospector;
import com.ogerardin.guarana.core.metamodel.ClassInformation;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;

/**
 * A factory to add observability to a POJO.
 */
@Slf4j
public enum ObservableFactory {
    ;

    /**
     * Returns a wrapper around the specified object that adds observability. The returned object will be of type T
     * and additionally implement {@link Observable}.
     * Any call to a setter for a property exposed by objectClass will fire a {@link java.beans.PropertyChangeEvent}
     * to registered listeners.
     * @param objectClass the object's class, or a superclass thereof. Only properties exposed by this class
     *                    will be observed.
     */
    public static <T> T createObservable(T object, Class<? super T> objectClass) {
        Enhancer enhancer = new Enhancer();

        // the object will be a subclass of T
        enhancer.setSuperclass(objectClass);
        // the object will also implement Observable
        enhancer.setInterfaces(new Class[] {Observable.class});
        // don't intercept method calls from constructor, otherwise we might end up modifying the wrapped object
        enhancer.setInterceptDuringConstruction(false);

        // Get information about specified class and provide custom callback to intercept method calls
        ClassInformation<? super T> classInformation = JavaIntrospector.getClassInformation(objectClass);
        enhancer.setCallback(new PropertyChangeInterceptor<>(object, classInformation));

        // Instanciate the wrapper
        //noinspection unchecked
        return (T) enhancer.create();
    }

    /**
     * Returns a wrapper around the specified object that adds observability. The returned object will be of type T
     * and additionally implement {@link Observable}.
     * Any call to a setter will fire a {@link java.beans.PropertyChangeEvent} to registered listeners.
     */
    public static <T> T createObservable(T object) {
        //noinspection unchecked
        return createObservable(object, (Class<? super T>) object.getClass());
    }
}
