/*
 *
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

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * @author Antti Salonen
 */
public abstract class WrapperComponent<DTOCLASS> extends CustomComponent implements Field {

    protected Logger log = LoggerFactory.getLogger(getClass());
    protected AbstractSelect field;
    protected Layout root;
    protected CaptionFormatter captionFormatter;
    protected FieldValueFormatter fieldValueFormatter;
    protected final IndexedContainer container = new IndexedContainer();

    protected WrapperComponent(CaptionFormatter<DTOCLASS> captionFormatter, FieldValueFormatter<DTOCLASS> fieldValueFormatter) {
        this.captionFormatter = captionFormatter;
        this.fieldValueFormatter = fieldValueFormatter;
        root = new HorizontalLayout();
        setCompositionRoot(root);
    }

    public void setField(AbstractSelect field) {
        this.field = field;
        container.removeAllItems();
        container.addContainerProperty("fieldCaption", String.class, "");
        field.setContainerDataSource(container);
        field.setItemCaptionPropertyId("fieldCaption");
        root.addComponent(field);
    }

    protected void setFieldValues() {
        List<DTOCLASS> dtos = loadOptions();
        for (DTOCLASS dto : dtos) {
            Object value = this.fieldValueFormatter.formatFieldValue(dto);
            Item item = container.addItem(value);
            if (item == null) {
                // this should happen only when closing view etc, but shouldn't matter anyway?
                //throw new NullPointerException("WrapperComponent.setFieldValues failed, null item for value: "+value+", dto: "+dto);
                log.warn("setFieldValues failed, null item for value: " + value + ", dto: " + dto);
            } else {
                String formattedCaption = formatCaption(dto);
                Property fieldCaption = item.getItemProperty("fieldCaption");
                fieldCaption.setValue(formattedCaption);
            }
        }
        container.sort(new Object[] {"fieldCaption"}, new boolean[] {true});
    }

    protected abstract List<DTOCLASS> loadOptions();

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        if (this.field == null) {
            throw new IllegalStateException("Field must be set before painting the component.");
        }
        super.paintContent(target);
    }

    /**
     * Formats displayed caption.
     *
     * @param dto
     * @return
     */
    protected String formatCaption(DTOCLASS dto) {
        return captionFormatter.formatCaption(dto);
    }

    /* (non-Javadoc)
    * @see com.vaadin.ui.AbstractComponentContainer#attach()
    */
    @Override
    public void attach() {
        super.attach();
        setFieldValues();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void focus() {
        super.focus();
    }

    @Override
    public boolean isInvalidCommitted() {
        return field.isInvalidCommitted();
    }

    @Override
    public void setInvalidCommitted(boolean isCommitted) {
        field.setInvalidAllowed(isCommitted);
    }

    @Override
    public void commit() {
        field.commit();
    }

    @Override
    public void discard() {
        field.discard();
    }

    @Override
    public boolean isWriteThrough() {
        return field.isWriteThrough();
    }

    @Override
    public void setWriteThrough(boolean writeThrough) {
        field.setWriteThrough(writeThrough);
    }

    @Override
    public boolean isReadThrough() {
        return field.isReadThrough();
    }

    @Override
    public void setReadThrough(boolean readThrough) {
        field.setReadThrough(readThrough);
    }

    @Override
    public boolean isModified() {
        return field.isModified();
    }

    @Override
    public void addValidator(Validator validator) {
        field.addValidator(validator);
    }

    @Override
    public void removeValidator(Validator validator) {
        field.removeValidator(validator);
    }

    @Override
    public Collection<Validator> getValidators() {
        return field.getValidators();
    }

    @Override
    public boolean isValid() {
        return field.isValid();
    }

    @Override
    public void validate() {
        field.validate();
    }

    @Override
    public boolean isInvalidAllowed() {
        return field.isInvalidAllowed();
    }

    @Override
    public void setInvalidAllowed(boolean invalidValueAllowed) {
        field.setInvalidAllowed(invalidValueAllowed);
    }

    @Override
    public Object getValue() {
        return field.getValue();
    }

    @Override
    public void setValue(Object newValue) {
        field.setValue(newValue);
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }

    @Override
    public void addListener(ValueChangeListener listener) {
        field.addListener(listener);
    }

    @Override
    public void removeListener(ValueChangeListener listener) {
        field.removeListener(listener);
    }

    @Override
    public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
        field.valueChange(event);
    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {
        field.setPropertyDataSource(newDataSource);
    }

    @Override
    public Property getPropertyDataSource() {
        return field.getPropertyDataSource();
    }

    @Override
    public int getTabIndex() {
        return field.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        field.setTabIndex(tabIndex);
    }

    @Override
    public boolean isRequired() {
        return field.isRequired();
    }

    @Override
    public void setRequired(boolean required) {
        field.setRequired(required);
    }

    @Override
    public void setRequiredError(String requiredMessage) {
        field.setRequiredError(requiredMessage);
    }

    @Override
    public String getRequiredError() {
        return field.getRequiredError();
    }

    @Override
    public boolean isImmediate() {
        return field.isImmediate();
    }

    @Override
    public void setImmediate(boolean immediate) {
        field.setImmediate(immediate);
    }

    /**
     * @param captionFormatter the captionFormatter to set
     */
    public void setCaptionFormatter(CaptionFormatter captionFormatter) {
        this.captionFormatter = captionFormatter;
    }

    /**
     * Sort container properties like {@link com.vaadin.data.Container.Sortable#sort(Object[], boolean[])}
     * @param propertyId
     * @param ascending
     */
    public void sort(Object[] propertyId, boolean[] ascending) {
        container.sort(propertyId, ascending);
    }

    public FieldValueFormatter getFieldValueFormatter() {
        return fieldValueFormatter;
    }

    public void setFieldValueFormatter(FieldValueFormatter fieldValueFormatter) {
        this.fieldValueFormatter = fieldValueFormatter;
    }

    public AbstractSelect getField() {
        return field;
    }

}
