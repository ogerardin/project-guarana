/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.Map;
import java.util.Set;

/**
 * A simple registry that maintains a bidirectional map of {@link Identifier} to {@link Object}.
 */
public enum ObjectRegistry {

    INSTANCE;

    private BiMap<Identifier, Object> map = HashBiMap.create();

    /**
     * Puts the specified object in the registry and returns the associated {@link Identifier}.
     * @return If the object was already present, the previsouly assigned Identifier; otherwise creates a new Identifier
     * and assigns it to the object.
     */
    public synchronized Identifier put(Object object) {
        Identifier identifier = map.inverse().get(object);
        if (identifier != null) {
            // object already in registry: return assigned ID
            return identifier;
        }
        // object not in registry: create unique ID and assign to object
        identifier = Identifier.getUniqueIdentifier();
        map.put(identifier, object);
        return identifier;
    }

    /**
     * @return The object to which the specified identifier is assigned, or null if there is no such object.
     */
    public Object get(Identifier identifier) {
        return map.get(identifier);
    }

    public Set<Map.Entry<Identifier, Object>> getEntrySet() {
        return map.entrySet();
    }

    public Map<Identifier, Object> getMap() {
        return map;
    }
}
