package com.ogerardin.guarana.core.ui;

import java.util.Collection;

/**
 * Created by oge on 08/09/2015.
 */
public interface CollectionUI<R, T> extends Renderable<R> {

    void setTarget(Collection<? extends T> target);

}
