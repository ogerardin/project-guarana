/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui.impl;

import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.ui.JfxMapUI;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implemetation of a MapUI for JavaFX
 *
 * @author Olivier
 * @since 29/05/15
 */
public class DefaultJfxMapUI<K, V> extends DefaultJfxCollectionUI<DefaultJfxMapUI.MapEntry> implements JfxMapUI<K, V> {

    private Map<K, V> target;

    public DefaultJfxMapUI(JfxUiManager builder) {
        super(builder, MapEntry.class);
    }

    @Override
    public void setTarget(Map<K, V> target) {
        super.setTarget(target.entrySet().stream()
                        .map(MapEntry<K, V>::new)
                        .collect(Collectors.toList())
        );
        this.target = target;
    }

    // HashMap.Entry is not public so we can't use it
    public static class MapEntry<K, V> {
        private K key;
        private V value;

        public MapEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public MapEntry(Map.Entry<K, V> me) {
            key = me.getKey();
            value = me.getValue();
        }

        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }
    }
}
