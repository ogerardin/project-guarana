package com.ogerardin.guarana.core.persistence.basic;

import com.ogerardin.guarana.core.persistence.PersistenceService;
import com.ogerardin.guarana.core.persistence.PersistenceServiceBuilder;

/**
 * Created by olivier on 18/05/2017.
 */
public class DefaultPersistenceServiceBuilder<C> implements PersistenceServiceBuilder<C> {

    @Override
    public PersistenceService<C> getPersistenceService(Class<C> clazz) {
        return new BasicPersistenceService<C>(clazz);
    }
}
