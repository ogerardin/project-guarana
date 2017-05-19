package com.ogerardin.guarana.core.persistance.basic;

import com.ogerardin.guarana.core.persistance.PersistenceService;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Most basic persistence service, implemented using native serialization.
 *
 * Created by olivier on 18/05/2017.
 */
public class BasicPersistenceService<C> implements PersistenceService<C> {

    private final Path path;

    public BasicPersistenceService(Class<C> clazz) {
        this.path = Paths.get(clazz.getName());
    }

    @Override
    public void save(C object) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(this.path.toFile()));
        outputStream.writeObject(object);
    }

    @Override
    public List<C> getAll() throws ClassNotFoundException, IOException {
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(this.path.toFile()));

        List<C> result = new ArrayList<>();
        while (true) {
            C object;
            try {
                object = (C) inputStream.readObject();
            } catch (EOFException e) {
                break;
            } catch (IOException e) {
                throw e;
            }
            result.add(object);
        }
        return result;
    }
}
