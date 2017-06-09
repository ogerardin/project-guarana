package com.ogerardin.guarana.core.persistance.basic;

import com.ogerardin.guarana.core.persistance.PersistenceService;
import com.ogerardin.guarana.core.persistance.PersistenceServiceBuilder;

/**
 * Created by olivier on 18/05/2017.
 */
public class DefaultPersistenceServiceBuilder<C> implements PersistenceServiceBuilder<C> {

    @Override
    public PersistenceService<C> getPersistenceService(Class<C> clazz) {
        return new BasicPersistenceService<C>(clazz);
    }
}
