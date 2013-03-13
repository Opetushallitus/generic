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

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;

public class OphRowMenuBar extends MenuBar {

    private MenuBar.MenuItem file;

    public OphRowMenuBar() {
        this.setWidth(-1, UNITS_PIXELS);
        addStyleName("treetable-dropdown-button");
        file = this.addItem("", null);
    }

    public OphRowMenuBar(String iconUrl) {
        this.setWidth(-1, UNITS_PIXELS);
        addStyleName("treetable-dropdown-button");
        file = this.addItem("", new ThemeResource(iconUrl), null);
    }

    public void addMenuCommand(String caption, Command command) {
        file.addItem(caption, command);
    }
}