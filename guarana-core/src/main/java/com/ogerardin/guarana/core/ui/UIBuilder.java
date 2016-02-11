/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.core.ui;

/**
 * Parametrized UI builder. Declares factory methods for common UI types: instance UI, collection UI, ...
 * @author oge
 * @since 07/09/2015
 * @param <R> actual type that Renderable objects will render to. This will depend on the target UI library;
 *           as a rule of thumb it should match a class that can be the root of an object hierarchy and can be itself
 *           put inside a high-level container.
 */
public interface UIBuilder<R> {

    /**
     * Builds a {@link InstanceUI} capable of representing an object of class C.
     */
    <C> InstanceUI<R, C> buildInstanceUI(Class<C> clazz);

    <C> InstanceUI<R, C> buildEmbeddedInstanceUI(Class<C> clazz);


    /**
     * Builds a {@link CollectionUI} capable of representing a collection of objects of class C.
     */
    <C> CollectionUI<R, C> buildCollectionUi(Class<C> clazz);

    /**
     * Builds a {@link MapUI} capable of representing a {@link java.util.Map}
     *
     * @param <K> type of map keys
     * @param <V> type of map values
     */
    <K, V> MapUI<R, K, V> buildMapUI();

}
