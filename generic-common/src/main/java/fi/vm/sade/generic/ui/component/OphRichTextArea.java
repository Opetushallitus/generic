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

import com.vaadin.data.validator.StringLengthValidator;
import org.vaadin.tinymceeditor.TinyMCETextField;

/**
 * Simple TinyMCETextField wrapper with default config and size.
 * <p/>
 * Default size:
 * <ul>
 * <li>Width: 600px</li>
 * <li>Height: 250px</li>
 * </ul>
 * <p/>
 * You can also limit the length of the text allowed in the component - if maxLength
 * is given a default "StringLengthValidator" is added to the component.
 *
 * @author mlyly
 */
public class OphRichTextArea extends TinyMCETextField {

    /**
     * Default configuration for OPH Rich Text Areas.
     */
    private static final String _config = "{theme : 'advanced', "
            + "theme_advanced_buttons1 : 'formatselect,bold,italic,underline,strikethrough,numlist,bullist,|,undo,redo,|,link,table,|,pastetext,pasteword,selectall,|,removeformat', "
            + "theme_advanced_buttons2 : '', "
            + "theme_advanced_buttons3 : '', "
            + "theme_advanced_statusbar_location : '', " 
            + "entity_encoding: 'raw'," 
            + "apply_source_formatting : false, "
            + "remove_linebreaks : true}";

    private static final String _readOnlyConfig = "{theme : 'advanced', mode : 'textareas', readonly : true}";

    /*
     * Max length for the data.
     */
    private int _maxLength = -1;

    public OphRichTextArea() {
        super();

        super.setConfig(_config);
        super.setWidth("600px");
        super.setHeight("250px");
        
    }

    /**
     * Create RTA that has a limited value length.
     *
     * @param maxLength set the maximum allowed length for text in the component
     * @param maxLengthExceededErrorMessage the error message to display when length is exceeded, can contain two
     *                                      parameters 0=current length, 1=max length
     */
    public OphRichTextArea(final int maxLength, String maxLengthExceededErrorMessage) {
        this();

        if (maxLength > 0) {
            // Add validator if max length is limited
            setMaxLength(maxLength + 1);
            addValidator(new StringLengthValidator(maxLengthExceededErrorMessage, 0, maxLength, true));
        }
    }

    /**
     * Note - overridden form the "TextField" - it does not work well with the TinyMCE.
     *
     * @param maxLength
     */
    @Override
    public void setMaxLength(int maxLength) {
        _maxLength = maxLength;
    }

    @Override
    public int getMaxLength() {
        return _maxLength;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        setConfig(readOnly ? _readOnlyConfig : _config);
    }

}
