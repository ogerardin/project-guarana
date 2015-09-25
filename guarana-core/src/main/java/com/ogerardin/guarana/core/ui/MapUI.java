package com.ogerardin.guarana.core.ui;

import java.util.Map;

/**
 * Created by oge on 08/09/2015.
 */
public interface MapUI<R, K, V> extends Renderable<R> {

    void setTarget(Map<K, V> target);

}
