/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("guarana")
public interface GuaranaService extends RemoteService {

    String getVersion();

    String introspect(String className) throws GuaranaServiceException;

    /**
     * Utility/Convenience class.
     * Use GuaranaService.App.getInstance() to access static instance of MySampleApplicationServiceAsync
     */
    class App {
        private static GuaranaServiceAsync ourInstance = GWT.create(GuaranaService.class);

        public static synchronized GuaranaServiceAsync getInstance() {
            return ourInstance;
        }
    }
}
