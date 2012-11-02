/*
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.generic.ui.component;

import com.vaadin.data.Property;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.themes.BaseTheme;
import java.util.ArrayList;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.customfield.CustomField;

/**
 * Simple "token selection" custom field.
 *
 * Displays a selection component and a list of selected items that can be selected and removed.
 *
 * This class contains many hooks "onXXX()"-methods for customizing the behavior.
 *
 * Example usage:
 * <pre>
 *   OphTokenField f = new OphTokenField();
 *   f.setPropertyDataSource(new BeanItem(model).getItemProperty("opetuskielet"));
 * </pre>
 *
 * Note: if you do setSelectionComponent it will not be added to this components layout. Note: if you do setSelectionLayout it will not be added to this
 * components layout.
 *
 * You can also separate selection field and list view yourself.
 * <pre>
 *   Layout l = new HorizontalLayout();
 *   addComponent(l); // Selection list updated here
 *
 *   KoodistoComponent kc = koodistoComboBox(null, KoodistoURIHelper.KOODISTO_KIELI_URI);
 *   kc.setImmediate(true);
 *   addComponent(kc); // manage selector placement
 *
 *   OphTokenField f = new OphTokenField();
 *   f.setSelectionComponent(kc);
 *   f.setSelectionLayout(l);
 *   f.setPropertyDataSource(new BeanItem(model).getItemProperty("opetuskielet"));
 *   addComponent(f);
 * </pre>
 *
 * @author mlyly
 */
public class OphTokenField extends CustomField {

    private static final Logger LOG = LoggerFactory.getLogger(OphTokenField.class);
    public static final String LAYOUT_STYLE = "TokenField_layout";
    public static final String SELECTION_LAYOUT_STYLE = "TokenField_selectionLayout";
    public static final String SELECTED_SINGLE_TOKEN_LAYOUT_STYLE = "TokenField_selectedSingleTokenLayout";
    /**
     * Formats tokens : "" + selectedToken
     */
    public static final SelectedTokenToTextFormatter DEFAULT_TOKEN_FORMATTER = new SelectedTokenToTextFormatter() {
        @Override
        public String formatToken(Object selectedToken) {
            return "" + selectedToken;
        }
    };
    /**
     * Makes sure that component is only initialized once.
     */
    private boolean _isAttached = false;
    /*
     * Layout that contains selection layout (if not given from outside)
     */
    private Layout _fieldLayout;

    /*
     * List of selected tokens rendered here.
     */
    private Layout _selectionLayout;

    /*
     * Selection component.
     */
    private Field _selectionComponent;

    /*
     * Default formatting for selected tokens.
     */
    private SelectedTokenToTextFormatter _formatter = DEFAULT_TOKEN_FORMATTER;

    public OphTokenField() {
        super();
        setCompositionRoot(new Label("TOKEN FIELD NOT INITIALIZED"));
    }

    /**
     * Defines how the list of selected tokens is formatted.
     *
     * @param formatter
     */
    public void setFormatter(SelectedTokenToTextFormatter formatter) {
        LOG.debug("setFormatter()");
        this._formatter = formatter;
    }

    /**
     * @return the selection component
     */
    public Field getSelectionComponent() {
        return _selectionComponent;
    }

    /**
     * Set component to use for selection
     *
     * @param selectionComponent
     */
    public void setSelectionComponent(Field selectionComponent) {
        LOG.debug("setSelectionComponent()");
        _selectionComponent = selectionComponent;
    }

    /**
     * @return component layout
     */
    public Layout getFieldLayout() {
        return _fieldLayout;
    }

    /**
     * Custom layout for component.
     *
     * @param fieldLayout
     */
    public void setFieldLayout(Layout fieldLayout) {
        LOG.debug("setFieldLayout()");
        _fieldLayout = fieldLayout;
    }

    /**
     * Selection layout, list of selected tokens.
     *
     * @return
     */
    public Layout getSelectionLayout() {
        return _selectionLayout;
    }

    /**
     * Selection layout, list of selected tokens added here.
     *
     * @param selectionLayout
     */
    public void setSelectionLayout(Layout selectionLayout) {
        LOG.debug("setSelectionLayout()");
        _selectionLayout = selectionLayout;
    }

    /**
     * Call this when a token needs to be deleted from the selected tokens list.
     *
     * @param selectedToken
     */
    public final void removeToken(Object selectedToken) {
        LOG.debug("removeToken({})", selectedToken);

        Collection values = (Collection) getValue();
        if (values != null) {
            values.remove(selectedToken);
        }

        fireValueChange(true);
    }

    @Override
    public void attach() {
        LOG.debug("attach()");
        super.attach();

        if (_isAttached) {
            return;
        }
        _isAttached = true;

        try {
            LOG.debug("  initializing...");

            // Not yet initialized, do it now

            // If selection and selection layout has been "given"/attached from outside - we will not do any layout/component adding.
            boolean addSelectionLayout = _selectionLayout == null || _selectionLayout.getApplication() == null;
            boolean addSelectionComponent = _selectionComponent == null || _selectionComponent.getApplication() == null;

            _fieldLayout = _fieldLayout != null ? _fieldLayout : onCreateComponentLayout();
            _selectionLayout = _selectionLayout != null ? _selectionLayout : onCreateSelectionLayout();
            _selectionComponent = _selectionComponent != null ? _selectionComponent : onCreateSelectionComponent();

            if (_fieldLayout == null || _selectionLayout == null || _selectionComponent == null) {
                setCompositionRoot(new Label("Field layout, selection layout or selectiomn component is null - cannot proceed."));
                return;
            }

            LOG.debug("  add selection component to field layout: {}", addSelectionComponent);
            if (addSelectionComponent) {
                _fieldLayout.addComponent(_selectionComponent);
            }

            LOG.debug("  add selection layout to field layout: {}", addSelectionLayout);
            if (addSelectionLayout) {
                _fieldLayout.addComponent(_selectionLayout);
            }

            setCompositionRoot(_fieldLayout);

            //
            // Wire up the selection from the select component
            //
            Property.ValueChangeListener selectionValueChangeListener = new Property.ValueChangeListener() {
                @Override
                public void valueChange(Property.ValueChangeEvent event) {
                    LOG.debug("  valueChange: event={}", event);

                    Collection values = (Collection) getValue();
                    if (values == null) {
                        LOG.debug("  token field old value was null, creating empty list.");
                        values = new ArrayList();
                        setValue(values);
                    }

                    if (values.contains(event.getProperty().getValue())) {
                        // Already in token list, nothing to do
                        LOG.debug("  already selected, wont add again.");
                        return;
                    }

                    if (onNewTokenSeleted(event.getProperty().getValue())) {
                        // OK, addition confirmed since not already in the selection list
                        values.add(event.getProperty().getValue());
                        fireValueChange(true);
                    }
                }
            };
            _selectionComponent.addListener(selectionValueChangeListener);


            // Wire up the selection change notification for redrawing.
            Property.ValueChangeListener valueChangeListener = new Property.ValueChangeListener() {
                @Override
                public void valueChange(Property.ValueChangeEvent event) {
                    LOG.debug("valueChange(event={})", event);
                    updateSelections();
                }
            };
            this.addListener(valueChangeListener);

            // Initial drawing - component may contain data already
            updateSelections();
        } catch (Throwable ex) {
            LOG.error("Failed to initialize OphTokenField component, exception:", ex);
            setCompositionRoot(new Label("FAILED TO INITIALIZED OphTokenField, see log for stacktrace!"));
        }
    }

    @Override
    public Class<?> getType() {
        return Collection.class;
    }

    /**
     * Redraw the selected tokens.
     */
    private void updateSelections() {
        LOG.debug("updateSelections()");

        if (_selectionLayout == null) {
            LOG.debug("  not yet initialized... cannot draw selections. Will be done in attach.");
            return;
        }

        _selectionLayout.removeAllComponents();

        Collection values = (Collection) getValue();
        if (values == null) {
            // Nothing to display
            return;
        }

        //
        // Loop over selected values, create selected component and add it to selection layout
        //
        for (final Object object : values) {
            Component selectedItem = onCreateSelectedTokenComponent(object);
            _selectionLayout.addComponent(selectedItem);
        }
    }

    /**
     * Called to create the selection component. This components will be hooked by this claass with value change listener to notify the method
     * onNewTokenSelected().
     *
     * By default creates new ComboBox component in immediate model with new items allowed, filtering mode contains.
     *
     * @return
     */
    protected AbstractSelect onCreateSelectionComponent() {
        LOG.debug("onCreateSelectionComponent()");

        ComboBox result = new ComboBox();
        result.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
        result.setImmediate(true);
        result.setNewItemsAllowed(true);
        return result;
    }

    /**
     * Called when component layout is created.
     *
     * By default returns new HorizontalLayout.
     *
     * @return
     */
    protected Layout onCreateComponentLayout() {
        LOG.debug("onCreateLayout()");

        // Create default layout
        HorizontalLayout l = new HorizontalLayout();
        l.setSpacing(true);
        l.addStyleName(LAYOUT_STYLE);
        return l;
    }

    /**
     * Called when new selection layout is created.
     *
     * @return
     */
    protected Layout onCreateSelectionLayout() {
        LOG.debug("onCreateSelectionLayout()");

        // Create default layout
        HorizontalLayout l = new HorizontalLayout();
        l.setSpacing(true);
        l.addStyleName(SELECTION_LAYOUT_STYLE);
        return l;
    }

    /**
     * Creates a selected item component.
     *
     * By default this methods creates Horizontal layout and two buttons.
     *
     * Buttons created are "link" styles and hooked up to:
     * <pre>
     * - onTokenDelete() method
     * - onSelectFromSelectedTokens() method
     * </pre>
     *
     * Component created by this method is plaved to "selectionLayout" created in in "onCreateSelectionLayout".
     *
     * @param selectedToken
     * @return
     */
    protected Component onCreateSelectedTokenComponent(final Object selectedToken) {
        LOG.debug("onCreateSelectedTokenComponent({})", selectedToken);

        HorizontalLayout singleTokenLayout = new HorizontalLayout();
        singleTokenLayout.addStyleName(SELECTED_SINGLE_TOKEN_LAYOUT_STYLE);

        // Create remove selection link
        Button removeTokenButton = new Button("(x)");
        removeTokenButton.addStyleName(BaseTheme.BUTTON_LINK);
        removeTokenButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                if (onTokenDelete(selectedToken)) {
                    removeToken(selectedToken);
                }
            }
        });

        // Create selected token as clickable link and hook the click to call onSelectFromSelectedTokens.
        Button selectTokenButton = new Button(_formatter.formatToken(selectedToken));
        selectTokenButton.addStyleName(BaseTheme.BUTTON_LINK);
        selectTokenButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                onSelectFromSelectedTokens(selectedToken);
            }
        });

        singleTokenLayout.addComponent(removeTokenButton);
        singleTokenLayout.addComponent(selectTokenButton);

        return singleTokenLayout;
    }

    /**
     * This method is called when a token is selected / clicked in the selected tokens list.
     *
     * By default this does nothing.
     *
     * @param selectedToken
     */
    protected void onSelectFromSelectedTokens(Object selectedToken) {
        LOG.debug("onSelectFromSelectedTokens({})", selectedToken);
    }

    /**
     * This method is called when deletion of a token is desired.
     *
     * If this method returns true, the token is deleted by calling the "removeToken" method.
     *
     * @param selectedToken
     * @return by default true
     */
    protected boolean onTokenDelete(Object selectedToken) {
        LOG.debug("onTokenDelete({})", selectedToken);
        return true;
    }

    /**
     * Returns true if selected token should be added to the selection list.
     *
     * @param tokenSelected
     * @return by default true for any non-null token. Return false to revoke addition.
     */
    protected boolean onNewTokenSeleted(Object tokenSelected) {
        LOG.debug("onNewTokenSelected({})", tokenSelected);
        return tokenSelected != null;
    }

    /**
     * Interface to format tokens.
     * Only used if you don't override "onSelectFromSelectedTokens"-method.
     */
    public interface SelectedTokenToTextFormatter {

        public String formatToken(Object selectedToken);
    }
}