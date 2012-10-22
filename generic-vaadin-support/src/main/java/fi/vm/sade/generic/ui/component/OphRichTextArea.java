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

import org.vaadin.tinymceeditor.TinyMCETextField;

/**
 * Siple TinyMCETextField wrapper with default config and size.
 *
 * @author mlyly
 */
public class OphRichTextArea extends TinyMCETextField {

    /**
     * Default configuration for OPH Rich Text Areas.
     */
    private static final String _config = "{theme : 'advanced', "
            + "theme_advanced_buttons1 : 'formatselect,bold,italic,underline,strikethrough,numlist,bullist,|,undo,redo,|,link,table,|,removeformat', "
            + "theme_advanced_buttons2 : '', "
            + "theme_advanced_buttons3 : '', "
            + "theme_advanced_statusbar_location : ''}";

    public OphRichTextArea() {
        super();

        super.setConfig(_config);
        super.setWidth("600px");
        super.setHeight("250px");
    }

}
