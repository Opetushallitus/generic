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

import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.*;
import fi.vm.sade.generic.common.I18N;
import fi.vm.sade.generic.common.LocalizedBusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.formbinder.ViewBoundForm;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;

/**
 * Custom form superclass that uses beanitem form inside.
 * Provides:
 *  - save button
 *  - save logic with exception handling
 *  - form <-> model binding
 *  - bind -method for setting the model/datasource
 *
 *  NOTE: override save and initFields -methods
 *
 * @author Antti Salonen
 */
public abstract class GenericForm<MODELCLASS> extends CustomComponent {

    private static final Logger LOG = LoggerFactory.getLogger(GenericForm.class);
    public static final String SAVE_SUCCESSFUL = "c_save_successful";
    protected Form form;
    protected Label serverMessage = new Label("");
    protected Button buttonSave;
    protected BeanItem<MODELCLASS> beanItem;

    public GenericForm(MODELCLASS model) {
        beanItem = new BeanItem<MODELCLASS>(model);

        // luodaan fieldit
        initFields();

        // luodaan form
        form = new ViewBoundForm(this);
        form.setFormFieldFactory(new PreCreatedFieldsHelper((new Object[]{
                this, form, ((ViewBoundForm) form).getCustomFieldSouces()
        })));

        // bindataan model <-> form
        form.setItemDataSource(beanItem);

        // luodaan save nappi
        buttonSave = createFormSaveButton();
        form.getFooter().addComponent(buttonSave);

        // add serverMessage label to ease automatic testing - TODO: pääleiskaan? piilotetuksi? ja pois tuotannosta..
        form.getFooter().addComponent(serverMessage);
    }

    protected Class<MODELCLASS> getModelClass() {
        return (Class<MODELCLASS>) ((ParameterizedType) (getClass().getGenericSuperclass())).getActualTypeArguments()[0];
    }

    protected abstract void initFields();

    private Button createFormSaveButton() {
        Button button = new Button(I18N.getMessage("save"), new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                processSave();
            }
        });
        return button;
    }

    protected void processSave() {
        try {
            LOG.debug("SAVEFORM commit...");
            form.commit();
            save((MODELCLASS) getBeanItem().getBean());
            form.getWindow().showNotification(I18N.getMessage("c_save_successful"));
            serverMessage.setValue(SAVE_SUCCESSFUL);
        } catch (Validator.EmptyValueException e) {
            LOG.warn("empty value, debugId: "+e.getDebugId()+", exception: "+e+", causes: "+ Arrays.asList(e.getCauses()), e);
            form.getWindow().showNotification("empty value: " + e.getDebugId(), Window.Notification.TYPE_ERROR_MESSAGE);
        } catch (LocalizedBusinessException e) {
            LOG.warn("encountered business exception when saving form: "+e, e);
            form.getWindow().showNotification(I18N.getMessage(e.getKey()), Window.Notification.TYPE_ERROR_MESSAGE);
        } catch (Throwable e) {
            LOG.error("encountered an exception when saving form: "+e, e);
            form.getWindow().showNotification(e.getLocalizedMessage(), Window.Notification.TYPE_ERROR_MESSAGE);
        }
    }

    protected BeanItem getBeanItem() {
        return ((BeanItem)form.getItemDataSource());
    }

    protected abstract MODELCLASS save(MODELCLASS model) throws Exception;

    public Button getButtonSave() {
        return buttonSave;
    }

    public Label getServerMessage() {
        return serverMessage;
    }

    public void bind(MODELCLASS model) {
        beanItem = new BeanItem<MODELCLASS>(model);
        form.setItemDataSource(beanItem);
    }

}
