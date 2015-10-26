/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GuaranaServiceAsync {

    void introspect(String className, AsyncCallback<String> async) throws GuaranaServiceException;

    void getVersion(AsyncCallback<String> async);

}
