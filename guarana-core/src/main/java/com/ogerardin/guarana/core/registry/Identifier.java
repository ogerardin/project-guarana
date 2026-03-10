/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.registry;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by oge on 16/10/2015.
 */
@ToString
@EqualsAndHashCode
public class Identifier implements Serializable {

    private final UUID uuid;

    private Identifier(UUID uuid) {
        this.uuid = uuid;
    }

    public static Identifier getUniqueIdentifier() {
        UUID uuid = UUID.randomUUID();
        return new Identifier(uuid);
    }
}
