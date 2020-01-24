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

/**
 * A {@link MethodInterceptor} that implements {@link Observable}, intercepts calls to setters to notify listeners,
 * and delegates all other method calls to the target object.
 * This was strongly inspired by https://dzone.com/articles/dynamic-class-enhancement-with-cglib and
 * http://markbramnik.blogspot.com/2010/04/cglib-introduction.html
 */
@Slf4j
class PropertyChangeInterceptor<T> implements MethodInterceptor {

    private final Object target;
    private final ClassInformation<T> classInformation;
    private final PropertyChangeSupport propertyChangeSupport;

    // Static Method instances created from method references using https://github.com/Hervian/safety-mirror
    private static final Method addPropertyChangeListenerMethod = Types.createMethod(Observable::addPropertyChangeListener);
    private static final Method removePropertyChangeListenerMethod = Types.createMethod(Observable::removePropertyChangeListener);


    PropertyChangeInterceptor(T target, ClassInformation<T> classInformation) {
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

        //Handle case where method is a setter
        Optional<PropertyInformation> maybeSetterTargetProperty = classInformation.propertyForSetter(method);
        if (maybeSetterTargetProperty.isPresent()) {
            PropertyInformation propertyInformation = maybeSetterTargetProperty.get();
            log.debug("setter invoked: " + method);

            //invoke getter to fetch previous value
            Method readMethod = propertyInformation.getReadMethod();
            Object oldValue = readMethod.invoke(target);
            //now invoke actual setter
            method.invoke(target, args);
            //fire property change
            propertyChangeSupport.firePropertyChange(propertyInformation.getName(), oldValue, args[0]);
            return null;
        };

        //General case: call method on wrapped object
        method.setAccessible(true);
        return method.invoke(target, args);
    }
}
