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
package fi.vm.sade.generic.ui.layout;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Styled Oppija layout which contains
 * left column (getter: getLeftArea(), cssclass: container-secondary), and
 * main column (getter: getMainArea(), cssclass: container-main).
 *
 * Add components like this: oppijaLayout.getContentArea().addComponent()
 * DO NOT USE: oppijaLayout.addComponent() - TODO: estä käyttö ja/tai komponenttien lisäykset omilla metodeilla esim addMainComponent?
 *
 * @author Antti Salonen
 */
public class OppijaLayout extends HorizontalLayout {

    private VerticalLayout leftArea = new VerticalLayout();
    private HorizontalLayout contentArea = new HorizontalLayout();

    public OppijaLayout() {
        leftArea.setStyleName("container-secondary"); // TODO: korvaa StyleNames.*
        contentArea.setStyleName("container-main");
        contentArea.addComponent(new Label(" "));
        super.addComponent(leftArea);
        super.addComponent(contentArea);
    }

    public VerticalLayout getLeftArea() {
        return leftArea;
    }

    public HorizontalLayout getContentArea() {
        return contentArea;
    }

}
