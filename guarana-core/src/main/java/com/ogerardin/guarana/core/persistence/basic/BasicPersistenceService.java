/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.persistence.basic;

import com.ogerardin.guarana.core.persistence.PersistenceService;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Most basic persistence service, implemented using native serialization.
 *
 * Created by olivier on 18/05/2017.
 */
public class BasicPersistenceService<C> implements PersistenceService<C> {

    private final Path path;

    public BasicPersistenceService(Class<C> clazz) {
        this.path = Paths.get(clazz.getName() + ".ser");
        this.path.toFile().deleteOnExit();
    }

    @Override
    public void save(C object) throws IOException, ClassNotFoundException {
        Set<C> allItems = getAll();
        allItems.add(object);
        saveAll(allItems);
    }

    @Override
    public Set<C> getAll() throws ClassNotFoundException, IOException {
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(this.path.toFile()));

        Set<C> result = new HashSet<>();
        while (true) {
            C object;
            try {
                //noinspection unchecked
                object = (C) inputStream.readObject();
            } catch (EOFException e) {
                break;
            }
            result.add(object);
        }
        return result;
    }

    private void saveAll(Set<C> items) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(this.path.toFile()));
        items.forEach(o -> {
            try {
                outputStream.writeObject(o);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void deleteAll() throws IOException {
        saveAll(Collections.emptySet());
    }

}
