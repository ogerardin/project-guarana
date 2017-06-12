/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.persistence;

import java.io.IOException;
import java.util.List;

/**
 * Created by olivier on 18/05/2017.
 */
public interface PersistenceService<C> {

    void save(C object) throws IOException;

    List<C> getAll() throws IOException, ClassNotFoundException;
}
