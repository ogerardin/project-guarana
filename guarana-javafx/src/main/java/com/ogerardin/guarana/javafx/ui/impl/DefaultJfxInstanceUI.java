/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.ogerardin.guarana.core.config.Util;
import com.ogerardin.guarana.core.introspection.JavaIntrospector;
import com.ogerardin.guarana.core.metamodel.ClassInformation;
import com.ogerardin.guarana.core.metamodel.PropertyInformation;
import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.binding.Bindings;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Default implementation of a InstanceUI for JavaFX.
 *
 * @param <T> type of the object being represented
 *
 * @author Olivier
 * @since 29/05/15
 */
public class DefaultJfxInstanceUI<T> extends JfxForm implements JfxInstanceUI<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultJfxInstanceUI.class);

    private BiMap<Node, PropertyInformation> propertyInformationByNode = HashBiMap.create();
    private BiMap<JfxInstanceUI, PropertyInformation> propertyInformationByUi = HashBiMap.create();

    private ObjectProperty<T> boundObjectProperty = new SimpleObjectProperty<T>();

    public DefaultJfxInstanceUI(JfxUiManager builder, Class<T> clazz) {
        super(builder);
        buildUi(clazz);
    }

    private void buildUi(Class<T> clazz) {

        ClassInformation<T> classInformation = JavaIntrospector.getClassInformation(clazz);

        // title
        final String displayName = getConfiguration().getClassDisplayName(clazz);
        final Label title = addTitle(displayName);
        configureDragSource(title, this::getBoundObject);
        configureContextMenu(title, classInformation, this::getBoundObject);

        // build properties form
        GridPane grid = buildGridPane();
        root.getChildren().add(grid);
        int row = 0;
        for (PropertyInformation propertyInformation : classInformation.getProperties()) {
            String propertyName = propertyInformation.getName();
            final Class<?> propertyType = propertyInformation.getPropertyType();
            final Method readMethod = propertyInformation.getReadMethod();

            // ignore hidden properties
            if (! getConfiguration().isShownProperty(classInformation.getJavaClass(), propertyName)) {
                continue;
            }

            // label
            final String humanizedName = Util.humanize(propertyInformation.getDisplayName());
            Label label = new Label(humanizedName);
            label.setTooltip(new Tooltip(propertyInformation.toString()));
            grid.add(label, 0, row);

            // field
            final Node field = buildPropertyUi(propertyInformation, propertyType);
            grid.add(field, 1, row);
            label.setLabelFor(field);


            // if it's a collection, add a button to open as list
            if (Collection.class.isAssignableFrom(propertyType)) {
                Button zoomButton = new Button("(+)");
                zoomButton.setOnAction(e -> zoomCollection(zoomButton, readMethod, humanizedName));
                grid.add(zoomButton, 2, row);
            }
            // if it's an array, add a button to open as a list only if element type is not primitive
            else if (propertyType.isArray()) {
                final Class<?> componentType = propertyType.getComponentType();
                if (!componentType.isPrimitive()) {
                    Button zoomButton = new Button("[+]");
                    zoomButton.setOnAction(e -> zoomArray(zoomButton, readMethod, componentType, humanizedName));
                    grid.add(zoomButton, 2, row);
                }
            }
            // otherwise if it's a zoomable type, add a button to zoom on property as single instance
            else if (getConfiguration().isZoomable(propertyType)) {
                Button zoomButton = new Button("...");
                zoomButton.setOnAction(e -> zoomProperty(zoomButton, propertyType, readMethod, humanizedName));
                grid.add(zoomButton, 2, row);
            }

            row++;
        }
    }


    private <P> Node buildPropertyUi(PropertyInformation propertyInformation, Class<P> propertyType) {

        JfxInstanceUI<P> ui = getBuilder().buildEmbeddedInstanceUI(propertyType);
        Node field = ui.getRendering();

        // set the field as a target for drag and drop
        configureDropTarget(field,
                (P value) -> propertyType.isAssignableFrom(value.getClass()),
                value -> ui.boundObjectProperty().setValue(value));

        propertyInformationByUi.put(ui, propertyInformation);
        propertyInformationByNode.put(field, propertyInformation);

        return field;
    }

    private <C> void zoomCollection(Node parent, Method readMethod, String title) {
        try {
            // Try to use generic introspection to determine the type of collection members.
            final Class<C> itemType = JavaIntrospector.getMethodResultSingleParameterType(readMethod);
            final Collection<C> collection = (Collection<C>) readMethod.invoke(getBoundObject());
            getBuilder().displayCollection(collection, itemType, parent, title);
        } catch (Exception e) {
            getBuilder().displayException(e);
        }
    }

    private <C> void zoomArray(Node parent, Method readMethod, Class<C> itemType, String title) {
        try {
            final C[] array = (C[]) readMethod.invoke(getBoundObject());
            getBuilder().displayArray(array, itemType, parent, title);
        } catch (Exception e) {
            getBuilder().displayException(e);
        }
    }

    private <P> void zoomProperty(Node parent, Class<P> propertyType, Method readMethod, String title) {
        try {
            final P value = (P) readMethod.invoke(getBoundObject());
            getBuilder().displayInstance(value, propertyType, parent, title);
        } catch (Exception ex) {
            getBuilder().displayException(ex);
        }
    }


    /**
     * Bind the specified object to this UI. This means:
     * - unbinding any previous bound object
     * - binding each property to the corresponding embedded UI
     * @param object
     */
    public void bind(T object) {
        LOGGER.debug("Binding " + object + " to " + this);
        if (getBoundObject() != null) {
            unbindProperties();
        }
        boundObjectProperty.set(object);
        bindProperties(object);
    }

    /**
     * Unbind each embedded UI
     */
    private void unbindProperties() {
        propertyInformationByUi.forEach(
                this::unbindProperty
        );
    }


    /**
     * Bind each property of the specified object to its corresponding embedded UI
     * @param object object providing property values
     */
    private void bindProperties(T object) {
        propertyInformationByUi.forEach(
                (ui, propertyInformation) -> bindProperty(object, ui, propertyInformation)
        );

    }


    private void unbindProperty(JfxInstanceUI ui, PropertyInformation propertyInformation) {
        //FIXME we should unbind bidirectionally
        ui.boundObjectProperty().unbind();
    }

    /**
     * @param object object providing property value
     * @param propertyUi embedded UI for the property
     * @param propertyInformation property description
     */
    private void bindProperty(T object, JfxInstanceUI propertyUi, PropertyInformation propertyInformation) {
        // Get the property value
        Object propertyValue;
        try {
            propertyValue = getPropertyValue(object, propertyInformation);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("failed to get value for property " + propertyInformation.getDisplayName(), e);
            return;
        }

        // if it's null and it's a collection, try to use an empty collection instead
        if (propertyValue == null) {
            //ui.boundObjectProperty().unbind();
            if (!propertyInformation.isCollection() || propertyInformation.getWriteMethod() == null) {
                return;
            }
            propertyValue = createEmptyCollection(propertyInformation);
            if (propertyValue == null) {
                return;
            }
            try {
                propertyInformation.getWriteMethod().invoke(object, propertyValue);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOGGER.error("failed to set value for property " + propertyInformation.getDisplayName(), e);
            }
        }

        final Class<?> valueClass = propertyValue.getClass();
        String propertyName = propertyInformation.getName();

        // if the property is a JavaFX-style property, bind directly to it
        if (Property.class.isAssignableFrom(valueClass)) {
            Property<?> jfxProperty = (Property) propertyValue;
            propertyUi.boundObjectProperty().bindBidirectional(jfxProperty);
            LOGGER.debug("[" + propertyName + "] bound using javafx.beans.property.Property method");
            return;
        }

        // otherwise try to bind bidirectionally to a generated JavaBeanObjectProperty
        try {
            Property<?> jfxSyntheticProperty = JavaBeanObjectPropertyBuilder.create()
                    .bean(object)
                    .name(propertyName)
                    .build();

            propertyUi.boundObjectProperty().bindBidirectional(jfxSyntheticProperty);
            LOGGER.debug("[" + propertyName + "] bound using JavaBeanObjectPropertyBuilder method");


            jfxSyntheticProperty.addListener((observable, oldValue, newValue) -> {
                LOGGER.debug("jfx property [" + propertyName +
                        "] changed: " + oldValue + " --> " + newValue);
            });

            propertyUi.boundObjectProperty().addListener((observable, oldValue, newValue) -> {
                LOGGER.debug("object bound to property [" + propertyName +
                        "] changed: " + oldValue + " --> " + newValue);
                //TODO notify change on this.boundobject
//                this.boundObjectProperty.setValue(this.boundObjectProperty.getValue());
            });


            return;
        } catch (NoSuchMethodException e) {
            // This happens when we try to use JavaBeanObjectPropertyBuilder on a read-only property
            LOGGER.debug("DEBUG: bindBidirectional threw NoSuchMethodException: " + e.toString());
        } catch (Exception e) {
            LOGGER.debug("DEBUG: bindBidirectional threw exception", e);
        }

        // otherwise if the property implements java.util.Observable, register a listener
        if (java.util.Observable.class.isAssignableFrom(valueClass)) {
            java.util.Observable observableValue = (java.util.Observable) propertyValue;
            final Observer observer = (observable, o) -> propertyUi.boundObjectProperty().setValue(observable);
            observableValue.addObserver(observer);
            observer.update(observableValue, this);
            LOGGER.debug("[" + propertyName + "] bound using java.util.Observable method");
            return;
        }

        // otherwise if the property implements javafx.beans.Observable, register a listener
        if (Observable.class.isAssignableFrom(valueClass)) {
            Observable observableValue = (Observable) propertyValue;
            final InvalidationListener listener = observable -> propertyUi.boundObjectProperty().setValue(observable);
            observableValue.addListener(listener);
            //FIXME listener is not called when list is changed subsequently; likely because change events are not invalidation events
            listener.invalidated(observableValue);
            LOGGER.debug("[" + propertyName + "] bound using javafx.beans.Observable method");
            return;
        }

        // otherwise just set the value
        LOGGER.warn("no binding for property [" + propertyName + "]");

        //ui.setReadOnly(true);
        propertyUi.bind(propertyValue);
    }

    private Object getPropertyValue(T object, PropertyInformation propertyInformation) throws IllegalAccessException, InvocationTargetException {
        Method readMethod = propertyInformation.getReadMethod();
        if (!readMethod.isAccessible()) {
            readMethod.setAccessible(true);
        }
        return readMethod.invoke(object);
    }

    /**
     * Try to instantiate a collection of the speficied type. Beware: the type may be an interface, in which case
     * we use a default implementation.
     */
    private Collection createEmptyCollection(PropertyInformation propertyInformation) {
        Class<? extends Collection> propertyType = (Class<? extends Collection>) propertyInformation.getPropertyType();


        if (propertyType.isInterface()) {
            propertyType = getDefaultCollectionImplementation(propertyType);
        }

        try {
            return propertyType.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.error("Failed to instantiate " + propertyType + " for null property");
            return null;
        }
    }

    private Class<? extends Collection> getDefaultCollectionImplementation(Class<? extends Collection> propertyType) {
        if (propertyType ==  List.class) {
            return ArrayList.class;
        }
        else if (propertyType == Set.class) {
            return HashSet.class;
        }
        else {
            throw new IllegalArgumentException("No default implementation for " + propertyType);
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        //TODO
    }

    protected void propertyUpdated(PropertyInformation propertyInformation, Object value) {
        Node node = propertyInformationByNode.inverse().get(propertyInformation);
        if (node instanceof TextField) {
            Bindings.fieldSetValue(getConfiguration(), ((TextField) node), propertyInformation, value);
        }
    }

    protected T getBoundObject() {
        return boundObjectProperty.get();
    }

    @Override
    public ObjectProperty<T> boundObjectProperty() {
        return boundObjectProperty;
    }
}
