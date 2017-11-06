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
import com.ogerardin.guarana.javafx.ui.impl.embedded.DefaultJfxEmbeddedInstanceUI;
import com.ogerardin.guarana.javafx.ui.impl.embedded.JfxDateUi;
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

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Default implementation of a InstanceUI for JavaFX. The UI is rendered by stacking vertically
 * an InstanceUI for each exposed property of the target class. By default, the InstanceUI used
 * to render a property is {@link DefaultJfxEmbeddedInstanceUI}, but this may be overridden in the
 * configuration by specifying the property guarana.class.targetClass.embeddedUiClass=uiClass, where
 * targetClass is the fully-qualified class name of the class to represent and uiClass is the
 * fully-qualified class name of the InstanceUI implementation to use. For example, the following
 * line instructs to use {@link JfxDateUi} as en embedded UI for properties of type {@link Date}:
 *
 * guarana.class.java.util.Date.embeddedUiClass=com.ogerardin.guarana.javafx.ui.impl.embedded.JfxDateUi
 *
 * @param <C> type of the object being represented
 *
 * @author Olivier
 * @since 29/05/15
 */
public class DefaultJfxInstanceUI<C> extends JfxForm implements JfxInstanceUI<C> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultJfxInstanceUI.class);

    private BiMap<Node, PropertyInformation> propertyInformationByNode = HashBiMap.create();
    private BiMap<JfxInstanceUI, PropertyInformation> propertyInformationByUi = HashBiMap.create();

    private ObjectProperty<C> boundObjectProperty = new SimpleObjectProperty<C>();

    public DefaultJfxInstanceUI(JfxUiManager builder, Class<C> clazz) {
        super(builder);
        buildUi(clazz);
    }

    private void buildUi(Class<C> clazz) {

        ClassInformation<C> classInformation = JavaIntrospector.getClassInformation(clazz);

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
                final Class<?> itemType = propertyType.getComponentType();
                if (!itemType.isPrimitive()) {
                    Button zoomButton = new Button("[+]");
                    zoomButton.setOnAction(e -> zoomArray(zoomButton, readMethod, itemType, humanizedName));
                    grid.add(zoomButton, 2, row);
                }
            }
            // otherwise if it's a zoomable type, add a button to zoom on property as single instance
            else if (getConfiguration().isZoomable(propertyType)) {
                Button zoomButton = new Button("...");
                zoomButton.setOnAction(e -> zoomProperty(zoomButton, readMethod, humanizedName));
                grid.add(zoomButton, 2, row);
            }

            row++;
        }
    }


    private <P> Node buildPropertyUi(PropertyInformation propertyInformation, Class<P> propertyType) {

        JfxInstanceUI<P> ui = getBuilder().buildEmbeddedInstanceUI(propertyType);
        Node field = ui.getRendered();

        // set the field as a target for drag and drop
        configureDropTarget(field,
                (P value) -> propertyType.isAssignableFrom(value.getClass()),
                (P value) -> ui.boundObjectProperty().setValue(value));

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

    private void zoomProperty(Node parent, Method readMethod, String title) {
        try {
            final Object value = readMethod.invoke(getBoundObject());
            Class runtimeClass = value.getClass();
            getBuilder().displayInstance(value, runtimeClass, parent, title);
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
    public void bind(C object) {
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
    private void bindProperties(C object) {
        propertyInformationByUi.forEach(
                (ui, propertyInformation) -> bindProperty(object, ui, propertyInformation)
        );

    }


    private void unbindProperty(JfxInstanceUI ui, PropertyInformation propertyInformation) {
        //FIXME we should unbind bidirectionally
        ui.boundObjectProperty().unbind();
    }

    /**
     * @param object object providing the property
     * @param propertyUi embedded UI for the property
     * @param propertyInformation property description
     */
    private void bindProperty(C object, JfxInstanceUI propertyUi, PropertyInformation propertyInformation) {
        // Get the property value
        Object propertyValue;
        try {
            propertyValue = getPropertyValue(object, propertyInformation.getPropertyDescriptor());
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("failed to get value for property " + propertyInformation.getDisplayName(), e);
            return;
        }

        // if it's null and it's a collection, try to use an empty collection instead
        if (propertyValue == null) {
            //ui.boundObjectProperty().unbind();
            if (!propertyInformation.isCollection() || propertyInformation.getWriteMethod() == null) {
                LOGGER.warn("Binding UI " + propertyUi + ": can't bind to null value");
                return;
            }
            propertyValue = createEmptyCollection(propertyInformation);
            if (propertyValue == null) {
                LOGGER.warn("Binding UI " + propertyUi + " to null collection: failed to provide empty collection");
                return;
            }
            LOGGER.warn("Binding UI " + propertyUi + " to null collection: providing empty collection");
            try {
                propertyInformation.getWriteMethod().invoke(object, propertyValue);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOGGER.error("failed to set value for property [" + propertyInformation.getName() + "]", e);
            }
        }

        final Class<?> valueClass = propertyValue.getClass();
        String propertyName = propertyInformation.getName();

        // if the property has an associated a JavaFX-style property, get its value (which is assumed to be of type
        // javafx.beans.property.Property) and bind directly to it
        if (propertyInformation.getJfxProperty() != null) {
            Property<?> jfxProperty;
            try {
                jfxProperty = (Property<?>) getPropertyValue(object, propertyInformation.getJfxProperty());
            } catch (Exception e) {
                LOGGER.error("failed to get value for JavaFX property " + propertyInformation.getJfxProperty().getName(), e);
                return;
            }
            propertyUi.boundObjectProperty().bindBidirectional(jfxProperty);
            LOGGER.debug("[" + propertyName + "] bound using javafx.beans.property.Property method");

            // DEBUG: trace change events on both UI field and object property
            jfxProperty.addListener((observable, oldValue, newValue) -> {
                LOGGER.debug("jfx property [" + propertyName + "] changed: " + oldValue + " --> " + newValue);
            });
            propertyUi.boundObjectProperty().addListener((observable, oldValue, newValue) -> {
                LOGGER.debug("object bound to property [" + propertyName + "] changed: " + oldValue + " --> " + newValue);
            });

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

            // DEBUG: trace change events on both UI field and object property
            jfxSyntheticProperty.addListener((observable, oldValue, newValue) -> {
                LOGGER.debug("jfx property [" + propertyName + "] changed: " + oldValue + " --> " + newValue);
            });
            propertyUi.boundObjectProperty().addListener((observable, oldValue, newValue) -> {
                LOGGER.debug("object bound to property [" + propertyName + "] changed: " + oldValue + " --> " + newValue);
            });

            return;
        } catch (NoSuchMethodException e) {
            // This happens when we try to use JavaBeanObjectPropertyBuilder on a read-only property
            LOGGER.warn("DEBUG: bindBidirectional threw NoSuchMethodException (read-only property?): " + e.toString());
        }

        // otherwise if the property implements java.util.Observable, register a listener
        if (java.util.Observable.class.isAssignableFrom(valueClass)) {
            java.util.Observable observableValue = (java.util.Observable) propertyValue;
            Observer observer = (observable, o) -> propertyUi.boundObjectProperty().setValue(observable);
            observableValue.addObserver(observer);
            observer.update(observableValue, this);
            LOGGER.debug("[" + propertyName + "] bound using java.util.Observable method");
            return;
        }

        // otherwise if the property implements javafx.beans.Observable, register a listener
        if (Observable.class.isAssignableFrom(valueClass)) {
            Observable observableValue = (Observable) propertyValue;
            InvalidationListener listener = observable -> propertyUi.boundObjectProperty().setValue(observable);
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

    private Object getPropertyValue(C object, PropertyDescriptor propertyDescriptor) throws IllegalAccessException, InvocationTargetException {
        Method readMethod = propertyDescriptor.getReadMethod();
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

    @Deprecated
    protected void propertyUpdated(PropertyInformation propertyInformation, Object value) {
        Node node = propertyInformationByNode.inverse().get(propertyInformation);
        if (node instanceof TextField) {
            Bindings.fieldSetValue(getConfiguration(), ((TextField) node), propertyInformation, value);
        }
    }

    protected C getBoundObject() {
        return boundObjectProperty.get();
    }

    @Override
    public ObjectProperty<C> boundObjectProperty() {
        return boundObjectProperty;
    }
}
