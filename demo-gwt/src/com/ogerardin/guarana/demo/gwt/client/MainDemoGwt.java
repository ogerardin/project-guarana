package com.ogerardin.guarana.demo.gwt.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.ogerardin.guarana.gwt.client.GuaranaService;
import com.ogerardin.guarana.gwt.client.GuaranaServiceException;

public class MainDemoGwt implements EntryPoint {

    public void onModuleLoad() {
        final TextBox textBox = new TextBox();
        final Button button = new Button("Introspect");
        final Label label = new Label("---");

        button.addClickHandler(event -> {
            try {
                GuaranaService.App.getInstance().introspect(textBox.getValue(), new MyAsyncCallback(label));
            } catch (GuaranaServiceException e) {
                e.printStackTrace();
            }
        });

        RootPanel.get("slot1").add(textBox);
        RootPanel.get("slot2").add(button);
        RootPanel.get("slot3").add(label);
    }

    private static class MyAsyncCallback implements AsyncCallback<String> {
        private final Label label;

        public MyAsyncCallback(Label label) {
            this.label = label;
        }

        @Override
        public void onFailure(Throwable caught) {
            label.setText(caught.toString());
        }

        @Override
        public void onSuccess(String result) {
            label.setText(result);
        }
    }
}
