/*
 * Copyright (c) 2015 Olivier Gérardin
 */

package com.ogerardin.guarana.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point class for the Core module
 */
public class CoreInit implements EntryPoint {

    public void onModuleLoad() {
        final Label label = new Label();

        GuaranaService.App.getInstance().getVersion(new MyAsyncCallback(label));

        RootPanel.get().add(label);
    }

    private static class MyAsyncCallback implements AsyncCallback<String> {
        private Label label;

        public MyAsyncCallback(Label label) {
            this.label = label;
        }

        public void onSuccess(String result) {
            label.setText("Powered by Guarana " + result);
        }

        public void onFailure(Throwable throwable) {
            label.setText("Failed to receive answer from server!");
        }
    }
}
