/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.persistence;

import java.io.IOException;
import java.util.Set;

/**
 * Service interface for persisting and retrieving objects of type C.
 * Implementations handle the storage and retrieval of domain objects.
 *
 * @param <C> the type of objects this service persists
 * @author Olivier Gérardin
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public interface PersistenceService<C> {

    /**
     * Saves one or more objects to persistent storage.
     */
    void save(C... objects) throws IOException, ClassNotFoundException;

    /**
     * Deletes one or more objects from persistent storage.
     */
    void delete(C... objects) throws IOException, ClassNotFoundException;

    /**
     * Retrieves all persisted objects.
     */
    Set<C> getAll() throws IOException, ClassNotFoundException;

    /**
     * Deletes all persisted objects.
     */
    void deleteAll() throws IOException;
}
