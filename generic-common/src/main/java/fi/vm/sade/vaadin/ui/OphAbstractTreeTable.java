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
package fi.vm.sade.vaadin.ui;


import com.vaadin.ui.TreeTable;
import fi.vm.sade.vaadin.Oph;
import fi.vm.sade.vaadin.util.UiBaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OphAbstractTreeTable extends TreeTable {

    private static final Logger LOG = LoggerFactory.getLogger(OphAbstractTreeTable.class);

    public OphAbstractTreeTable() {
        super();

        init(null, null);
    }

    public OphAbstractTreeTable(String width, String height) {
        super();

        init(width, height);
    }

    private void init(String width, String height) {
        addStyleName(Oph.TABLE_BORDERLESS); //TODO: TEEMATKAA!
    
        // OTHER
        setSelectable(false);
        setImmediate(false);

        if (width != null || height != null) {
            UiBaseUtil.handleWidth(this, width);
            UiBaseUtil.handleHeight(this, height);
        } else {
            setSizeFull();
        }
    }
}