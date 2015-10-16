/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.object_registry;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by oge on 16/10/2015.
 */
public enum ObjectRegistry {

    INSTANCE;

    private Map<Identifier, Object> identifierToObjectMap = new HashMap<>();
    private Map<Object, Identifier> objectToIdentifierMap = new HashMap<>();

    public synchronized Identifier put(Object object) {
        Identifier identifier = objectToIdentifierMap.get(object);
        if (identifier != null) {
            // object already in registry: return assigned ID
            return identifier;
        }
        // object not in registry: create unique ID and assign to object
        identifier = Identifier.getUniqueIdentifier();
        identifierToObjectMap.put(identifier, object);
        objectToIdentifierMap.put(object, identifier);

        return identifier;
    }

    public Object get(Identifier identifier) {
        return identifierToObjectMap.get(identifier);
    }
}
