/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui;

import com.ogerardin.guarana.core.ui.MapUI;
import javafx.scene.Parent;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Olivier on 29/05/15.
 */
public class JfxMapUI<K, V> extends JfxCollectionUI<JfxMapUI.MapEntry> implements MapUI<Parent, K, V> {

    private Map<K, V> target;

    public JfxMapUI() {
        super(MapEntry.class);
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
