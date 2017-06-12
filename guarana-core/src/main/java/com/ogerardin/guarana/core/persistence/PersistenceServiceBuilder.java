/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.persistence;

/**
 * Created by olivier on 18/05/2017.
 */
public interface PersistenceServiceBuilder<C> {

    PersistenceService<C> getPersistenceService(Class<C> clazz);
}
