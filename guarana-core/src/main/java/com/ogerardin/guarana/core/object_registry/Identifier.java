/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.object_registry;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by oge on 16/10/2015.
 */
public class Identifier implements Serializable {

    private final UUID uuid;

    private Identifier(UUID uuid) {
        this.uuid = uuid;
    }

    public static Identifier getUniqueIdentifier() {
        UUID uuid = UUID.randomUUID();
        return new Identifier(uuid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identifier that = (Identifier) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public String toString() {
        return "Identifier{" +
                "uuid=" + uuid +
                '}';
    }
}
