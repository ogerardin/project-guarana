/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GuaranaServiceAsync {
    void getMessage(String msg, AsyncCallback<String> async);

    void introspect(String className, AsyncCallback<String> async) throws GuaranaServiceException;
}
