/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.persistence;

import java.io.IOException;
import java.util.Set;

/**
 * Created by olivier on 18/05/2017.
 */
public interface PersistenceService<C> {

    void save(C... objects) throws IOException, ClassNotFoundException;

    void delete(C... objects) throws IOException, ClassNotFoundException;

    Set<C> getAll() throws IOException, ClassNotFoundException;

    void deleteAll() throws IOException;
}
