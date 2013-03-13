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

import com.vaadin.ui.HorizontalLayout;
import fi.vm.sade.vaadin.constants.UiConstant;
import fi.vm.sade.vaadin.constants.UiMarginEnum;
import fi.vm.sade.vaadin.util.UiBaseUtil;

/**
 *
 * @author jani
 */
public class OphHorizontalLayout extends HorizontalLayout {
    private static final long serialVersionUID = -3023094902374835480L;

    public OphHorizontalLayout() {
        super();

        init(false, UiMarginEnum.NONE, null, UiConstant.DEFAULT_RELATIVE_SIZE);
    }

    public OphHorizontalLayout(boolean spacing, UiMarginEnum margin) {
        super();

        init(spacing, margin, null, UiConstant.DEFAULT_RELATIVE_SIZE);
    }

    public OphHorizontalLayout(boolean spacing, UiMarginEnum margin, String width, String height) {
        super();

        init(spacing, margin, width, height);
    }

    private void init(boolean spacing, UiMarginEnum margin, String width, String height) {
        this.setSpacing(spacing);
        UiBaseUtil.handleWidth(this, width);
        UiBaseUtil.handleHeight(this, height);
        UiBaseUtil.handleMarginParam(this, margin != null ? margin.getSelectedValue() : null);
    }
}
