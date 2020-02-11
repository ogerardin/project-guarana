/*
 * Copyright (c) 2017 Olivier Gérardin
 */

package com.ogerardin.guarana.javafx.ui.impl;

import com.ogerardin.guarana.core.config.Util;
import com.ogerardin.guarana.core.introspection.JavaIntrospector;
import com.ogerardin.guarana.core.metamodel.ClassInformation;
import com.ogerardin.guarana.core.metamodel.PropertyInformation;
import com.ogerardin.guarana.core.observability.ObservableFactory;
import com.ogerardin.guarana.javafx.JfxUiManager;
import com.ogerardin.guarana.javafx.binding.BindingStrategy;
import com.ogerardin.guarana.javafx.binding.strategies.*;
import com.ogerardin.guarana.javafx.ui.JfxInstanceUI;
import com.ogerardin.guarana.javafx.ui.impl.embedded.DefaultJfxEmbeddedInstanceUI;
import com.ogerardin.guarana.javafx.ui.impl.embedded.JfxDateUi;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
@Slf4j
public class DefaultJfxInstanceUI<C> extends JfxForm implements JfxInstanceUI<C>, PropertyChangeListener {

    private static final List<BindingStrategy> BINDING_STRATEGIES = Arrays.asList(
            new JfxBindingStrategy(),
            new JfxObservableListBindingStrategy(),
            new JfxObservableBindingStrategy(),
            new JavaObservableBindingStrategy(),
            new JavaBeanObjectPropertyBuilderStrategy()
    );

    private final Map<String, UIPropertyInfo> propertyNameToPropertyInfo = new HashMap<>();

    private final ObjectProperty<C> boundObjectProperty = new SimpleObjectProperty<C>();

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
                Button zoomButton = new Button("...");
                zoomButton.setOnAction(e -> zoomCollection(zoomButton, propertyInformation, humanizedName));
                grid.add(zoomButton, 2, row);
            }
            // if it's an array, add a button to open as a list only if element type is not primitive
            else if (propertyType.isArray()) {
                final Class<?> itemType = propertyType.getComponentType();
                if (!itemType.isPrimitive()) {
                    Button zoomButton = new Button("...");
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

    private <P> void displayProperty(C object, JfxInstanceUI<P> ui, PropertyInformation propertyInformation) {
        // Get the property value
        P propertyValue;
        try {
            //noinspection unchecked
            propertyValue = (P) PropertyUtils.getSimpleProperty(object, propertyInformation.getPropertyDescriptor().getName());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("failed to get value for property " + propertyInformation.getDisplayName(), e);
            return;
        }

        ui.display(propertyValue);
    }

    private <P> void populateProperty(C object, JfxInstanceUI<P> ui, PropertyInformation propertyInformation) {
        final P uiValue = ui.boundObjectProperty().get();
        final Method writeMethod = propertyInformation.getWriteMethod();
        if (writeMethod != null) {
            try {
                writeMethod.invoke(object, uiValue);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }


    private <P> Node buildPropertyUi(PropertyInformation propertyInformation, Class<P> propertyType) {

        JfxInstanceUI<P> ui = getBuilder().buildEmbeddedInstanceUI(propertyType);
        Node field = ui.getRendered();

        // set the field as a target for drag and drop
        configureDropTarget(field,
                (P value) -> propertyType.isAssignableFrom(value.getClass()),
                (P value) -> ui.boundObjectProperty().setValue(value));

        propertyNameToPropertyInfo.put(
                propertyInformation.getName(),
                new UIPropertyInfo(propertyInformation, ui)
        );

        return field;
    }

    private <I> void zoomCollection(Node parent, PropertyInformation propertyInformation, String title) {
        try {
            // Try to use generic introspection to determine the type of collection members.
            final Method readMethod = propertyInformation.getReadMethod();
            final Class<I> itemType = JavaIntrospector.getMethodResultSingleParameterType(readMethod);
            Collection<I> originalCollection = (Collection<I>) readMethod.invoke(getBoundObject());
            Collection<I> collection = originalCollection;

            if (collection == null) {
                collection = createEmptyCollection(propertyInformation);
            }
            if (! (collection instanceof Observable)) {
                collection = makeObervable(collection);
            }

            if (collection != originalCollection) {
                propertyInformation.getWriteMethod().invoke(getBoundObject(), collection);
            }

            getBuilder().displayCollection(collection, itemType, parent, title);
        } catch (Exception e) {
            getBuilder().displayException(e);
        }
    }

    private <I> void zoomArray(Node parent, Method readMethod, Class<I> itemType, String title) {
        try {
            final I[] array = (I[]) readMethod.invoke(getBoundObject());
            getBuilder().displayArray(array, itemType, parent, title);
        } catch (Exception e) {
            getBuilder().displayException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <P> void zoomProperty(Node parent, Method readMethod, String title) {
        try {
            final P value = (P) readMethod.invoke(getBoundObject());
            Class<P> runtimeClass = (Class<P>) value.getClass();
            getBuilder().displayInstance(value, runtimeClass, parent, title);
        } catch (Exception ex) {
            getBuilder().displayException(ex);
        }
    }


    /**
     * Bind the specified object to this UI. This means:
     * - unbinding any previous bound object
     * - binding each property to the corresponding embedded UI
     */
    public void bind(C object) {
        log.debug("Binding " + object + " to " + this);
        unbind();

        if (! (object instanceof com.ogerardin.guarana.core.observability.Observable)) {
            object = ObservableFactory.createObservable(object);
        }

        ((com.ogerardin.guarana.core.observability.Observable) object).addPropertyChangeListener(
                this
        );

        boundObjectProperty.set(object);
        bindProperties(object);
    }

    private void unbind() {
        final C boundObject = getBoundObject();
        if (boundObject != null) {
            unbindProperties();
            boundObjectProperty.set(null);
        }
    }

    /**
     * Display the specified object in this UI. This means:
     * - unbinding any previous bound object
     * - setting the value of each embedded UI to the corresponding property value
     * No binding is performed, i.e. changes are not immediately propagated to the specified object.
     */
    @Override
    public void display(C object) {
        log.debug("Displaying {}", object);
        unbind();
        displayProperties(object);
    }

    @Override
    public void populate(C object) {
        populateProperties(object);
    }

    private void populateProperties(C object) {
        propertyNameToPropertyInfo.values()
                .forEach(v -> populateProperty(object, v.getJfxInstanceUI(), v.getPropertyInformation()));
    }

    /**
     * Unbind each embedded UI
     */
    private void unbindProperties() {
        propertyNameToPropertyInfo.values()
                .forEach(v -> unbindProperty(v.getJfxInstanceUI()));
    }


    /**
     * Bind each property of the specified object to its corresponding embedded UI
     * @param object object providing property values
     */
    private void bindProperties(C object) {
        propertyNameToPropertyInfo.values()
                .forEach(v -> bindProperty(object, v.getJfxInstanceUI(), v.getPropertyInformation()));

    }

    private void displayProperties(C object) {
        propertyNameToPropertyInfo.values()
                .forEach(v -> displayProperty(object, v.getJfxInstanceUI(), v.getPropertyInformation()));

    }


    private <P> void unbindProperty(JfxInstanceUI<P> ui) {
        //FIXME we should unbind bidirectionally
        ui.boundObjectProperty().unbind();
    }

    /**
     * @param object object providing the property
     * @param propertyUi embedded UI for the property
     * @param propertyInformation property description
     */
    private <P> void bindProperty(C object, JfxInstanceUI<P> propertyUi, PropertyInformation propertyInformation) {
        // Get the property value
        P propertyValue;
        try {
            //noinspection unchecked
            propertyValue = (P) PropertyUtils.getSimpleProperty(object, propertyInformation.getPropertyDescriptor().getName());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("failed to get value for property " + propertyInformation.getDisplayName(), e);
            return;
        }

        String propertyName = propertyInformation.getName();
        for (BindingStrategy strategy : BINDING_STRATEGIES) {
            try {
                strategy.bind(object, propertyUi, propertyInformation, propertyValue);
                log.debug("Property [{}] bound using strategy {}", propertyName, strategy.getClass());
                return;
            } catch (Exception ignored) {
            }
        }

        // no strategy succeeded: just set the value
        log.warn("no binding for property [" + propertyName + "]");
        //ui.setReadOnly(true);
        propertyUi.bind(propertyValue);
    }

    /**
     * Try to instantiate a collection of the speficied type. Beware: the type may be an interface, in which case
     * we use a default implementation.
     */
    private <I> Collection<I> createEmptyCollection(PropertyInformation propertyInformation) throws IllegalAccessException, InstantiationException {
        Class<I> propertyType = (Class<I>) propertyInformation.getPropertyType();

        if (propertyType.isInterface()) {
            if (propertyType == List.class) {
                return new ArrayList<>();
            }
            if (propertyType == Set.class) {
                return new HashSet<>();
            }
            throw new IllegalArgumentException("No default implementation for " + propertyType);
        }

        try {
            final Collection<?> collection = (Collection<?>) propertyType.newInstance();
            return (Collection<I>) collection;
        }
        catch (InstantiationException | IllegalAccessException e) {
            log.error("Failed to instantiate " + propertyType);
            throw e;
        }
    }

    private <I> Collection<I> makeObervable(Collection<I> collection) {
        if (collection instanceof List<?>) {
            return FXCollections.observableList((List<I>) collection);
        }
        if (collection instanceof Set<?>) {
            return FXCollections.observableSet((Set<I>) collection);
        }
        log.warn("Don't know how to make an observable of " + collection);
        return collection;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        //TODO
    }

    protected C getBoundObject() {
        return boundObjectProperty.get();
    }

    @Override
    public ObjectProperty<C> boundObjectProperty() {
        return boundObjectProperty;
    }

    // implementation of PropertyChangeListener
    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        log.debug("property change notified: {}", propertyChangeEvent);
        // a property has changed, we need to unbind the UI from the previous value
        // and rebind it to the new value
        final String propertyName = propertyChangeEvent.getPropertyName();
        log.debug("Rebinding property [{}] to new value {}", propertyName, propertyChangeEvent.getNewValue());
        final UIPropertyInfo uiPropertyInfo = propertyNameToPropertyInfo.get(propertyName);
        final JfxInstanceUI<?> ui = uiPropertyInfo.getJfxInstanceUI();
        final PropertyInformation propertyInformation = uiPropertyInfo.getPropertyInformation();
        unbindProperty(ui);
        bindProperty(getBoundObject(), ui, propertyInformation);

    }

    @Data
    private static class UIPropertyInfo {
        private final PropertyInformation propertyInformation;
        private final JfxInstanceUI<?> jfxInstanceUI;
    }
}
