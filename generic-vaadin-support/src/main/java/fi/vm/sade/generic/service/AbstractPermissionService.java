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
package fi.vm.sade.generic.service;

import fi.vm.sade.generic.ui.portlet.security.User;

/**
 * Abstract base class for UI permission checks
 *
 * @author wuoti
 *
 */
public abstract class AbstractPermissionService implements PermissionService {

    @Override
    public boolean userCanRead() {
        return getUser().isUserInRole(getReadRole()) || userCanReadAndUpdate() || userCanCreateReadUpdateAndDelete();
    }

    @Override
    public boolean userCanReadAndUpdate() {
        return getUser().isUserInRole(getReadUpdateRole()) || userCanCreateReadUpdateAndDelete();
    }

    @Override
    public boolean userCanCreateReadUpdateAndDelete() {
        return getUser().isUserInRole(getCreateReadUpdateDeleteRole());
    }

    protected abstract String getReadRole();

    protected abstract String getReadUpdateRole();

    protected abstract String getCreateReadUpdateDeleteRole();

    protected abstract User getUser();
}
