/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.gwt.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.ogerardin.guarana.gwt.client.GuaranaService;
import com.ogerardin.guarana.gwt.client.GuaranaServiceException;

public class GuaranaServiceImpl extends RemoteServiceServlet implements GuaranaService {
    // Implementation of sample interface method
    public String getMessage(String msg) {
        return "Client said: \"" + msg + "\"<br>Server answered: \"Hi!\"";
    }

    @Override
    public String introspect(String className) throws GuaranaServiceException {
        Class clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new GuaranaServiceException(e.toString());
        }
        return clazz.toString();
    }

}