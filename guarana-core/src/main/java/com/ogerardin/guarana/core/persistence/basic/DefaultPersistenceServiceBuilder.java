/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.persistence.basic;

import com.ogerardin.guarana.core.persistence.PersistenceService;
import com.ogerardin.guarana.core.persistence.PersistenceServiceBuilder;

/**
 * Default implementation of {@link PersistenceServiceBuilder} that creates
 * {@link BasicPersistenceService} instances.
 *
 * @param <C> the type of objects to be persisted
 * @author Olivier Gérardin
 * @since 1.0
 */
public class DefaultPersistenceServiceBuilder<C> implements PersistenceServiceBuilder<C> {

    @Override
    public PersistenceService<C> getPersistenceService(Class<C> clazz) {
        return new BasicPersistenceService<C>(clazz);
    }
}
