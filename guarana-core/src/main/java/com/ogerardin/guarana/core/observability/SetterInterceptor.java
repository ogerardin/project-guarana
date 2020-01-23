package com.ogerardin.guarana.core.observability;

import com.ogerardin.guarana.core.metamodel.ClassInformation;
import com.ogerardin.guarana.core.metamodel.PropertyInformation;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Optional;

@Slf4j
public class SetterInterceptor<T> implements MethodInterceptor {

    private final Object target;
    private final ClassInformation<T> classInformation;

    public SetterInterceptor(T target, ClassInformation<T> classInformation) {
        this.target = target;
        this.classInformation = classInformation;
    }

    public Object intercept(Object o,
                            Method method,
                            Object[] args,
                            MethodProxy proxy) throws Throwable {
        method.setAccessible(true);
//                log.debug("invoked on proxy: " + method);
        final Object result = method.invoke(target, args);
        final Optional<PropertyInformation> maybePropertyInformation = classInformation.isSetterForProperty(method);
        maybePropertyInformation.ifPresent(propertyInformation -> {
            log.debug("setter invoked for property: " + maybePropertyInformation.get());
            //TODO fire change event to observers, see https://dzone.com/articles/dynamic-class-enhancement-with-cglib
        });
        return result;
    }
}
