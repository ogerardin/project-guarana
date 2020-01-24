package com.ogerardin.guarana.core.observability;

import com.github.hervian.reflection.Types;
import com.ogerardin.guarana.core.metamodel.ClassInformation;
import com.ogerardin.guarana.core.metamodel.PropertyInformation;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;
import java.util.Optional;

@Slf4j
public class PropertyChangeInterceptor<T> implements MethodInterceptor {

    private final Object target;
    private final ClassInformation<T> classInformation;
    private final PropertyChangeSupport propertyChangeSupport;

    private static final Method addPropertyChangeListenerMethod = Types.createMethod(Observable::addPropertyChangeListener);
    private static final Method removePropertyChangeListenerMethod = Types.createMethod(Observable::removePropertyChangeListener);


    public PropertyChangeInterceptor(T target, ClassInformation<T> classInformation) {
        this.target = target;
        this.classInformation = classInformation;
        this.propertyChangeSupport = new PropertyChangeSupport(target);
    }

    public Object intercept(Object o, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        // Observable implementation
        if (method.equals(addPropertyChangeListenerMethod)) {
            propertyChangeSupport.addPropertyChangeListener((PropertyChangeListener) args[0]);
            return null;
        }
        if (method.equals(removePropertyChangeListenerMethod)) {
            propertyChangeSupport.removePropertyChangeListener((PropertyChangeListener) args[0]);
            return null;
        }

//        method.setAccessible(true);
//                log.debug("invoked on proxy: " + method);
        Optional<PropertyInformation> maybePropertyInformation = classInformation.isSetterForProperty(method);

        //Handle case where method is a setter
        if (maybePropertyInformation.isPresent()) {
            PropertyInformation propertyInformation = maybePropertyInformation.get();
            log.debug("setter invoked for property: " + propertyInformation);
            // see https://dzone.com/articles/dynamic-class-enhancement-with-cglib

            //invoke getter to fetch previous
            Method readMethod = propertyInformation.getReadMethod();
            Object oldValue = readMethod.invoke(target);
            //now invoke setter
            method.invoke(target, args);
            //fire property change
            propertyChangeSupport.firePropertyChange(propertyInformation.getName(), oldValue, args[0]);
            return null;
        };

        //Handle general case
        return method.invoke(target, args);
    }
}
