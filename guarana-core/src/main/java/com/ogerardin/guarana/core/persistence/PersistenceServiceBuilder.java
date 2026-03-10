/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.persistence;

/**
 * Builder interface for creating {@link PersistenceService} instances.
 *
 * @param <C> the type of objects to be persisted
 * @author Olivier Gérardin
 * @since 1.0
 */
public interface PersistenceServiceBuilder<C> {

    /**
     * Creates and returns a {@link PersistenceService} for the specified class.
     */
    PersistenceService<C> getPersistenceService(Class<C> clazz);
}
