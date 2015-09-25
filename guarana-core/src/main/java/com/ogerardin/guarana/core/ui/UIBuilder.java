package com.ogerardin.guarana.core.ui;

/**
 * Created by oge on 07/09/2015.
 */
public interface UIBuilder<R> {

    ClassUI<R> buildClassUI(Class clazz);

    <C> InstanceUI<R, C> buildInstanceUI(Class<C> clazz);

    <C> CollectionUI<R, C> buildCollectionUi(Class<C> clazz);

    <K, V> MapUI<R, K, V> buildMapUI();

}
