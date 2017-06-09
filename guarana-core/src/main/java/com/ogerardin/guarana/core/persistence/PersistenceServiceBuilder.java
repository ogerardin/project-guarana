package com.ogerardin.guarana.core.persistance;

/**
 * Created by olivier on 18/05/2017.
 */
public interface PersistenceServiceBuilder<C> {

    PersistenceService<C> getPersistenceService(Class<C> clazz);
}
