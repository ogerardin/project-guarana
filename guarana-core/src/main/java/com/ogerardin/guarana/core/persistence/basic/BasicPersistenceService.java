/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.core.persistence.basic;

import com.ogerardin.guarana.core.persistence.PersistenceService;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.ogerardin.guarana.core.java.LambdaExceptionUtil.rethrowingConsumer;

/**
 * Most basic persistence service, implemented using native serialization.
 *
 * Created by olivier on 18/05/2017.
 */
public class BasicPersistenceService<C> implements PersistenceService<C> {

    private static final String DATAFILE_SUFFIX = ".ser";

    private final Path dataFilePath;

    public BasicPersistenceService(Path baseDirPath, Class<C> clazz) {
        this.dataFilePath = baseDirPath.resolve(clazz.getName() + DATAFILE_SUFFIX);
        this.dataFilePath.toFile().deleteOnExit();
    }

    public BasicPersistenceService(Class<C> clazz) {
        this(Paths.get(""), clazz);
    }

    @SafeVarargs
    @Override
    public final void save(C... objects) throws IOException, ClassNotFoundException {
        Set<C> allItems = getAll();
        allItems.addAll(Arrays.asList(objects));
        saveAll(allItems);
    }

    @SafeVarargs
    @Override
    public final void delete(C... objects) throws IOException, ClassNotFoundException {
        Set<C> allItems = getAll();
        allItems.removeAll(Arrays.asList(objects));
        saveAll(allItems);
    }

    @Override
    public Set<C> getAll() throws IOException, ClassNotFoundException {
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(this.dataFilePath.toFile()));

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
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(this.dataFilePath.toFile()));
        items.forEach(rethrowingConsumer(outputStream::writeObject));
    }

    @Override
    public void deleteAll() throws IOException {
        saveAll(Collections.emptySet());
    }

}
