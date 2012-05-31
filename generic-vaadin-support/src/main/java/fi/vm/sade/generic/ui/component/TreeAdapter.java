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

import java.util.Collection;

/**
 * Helps SearchableTree component to define node id, parent and caption.
 *
 * @author Antti Salonen
 */
public interface TreeAdapter<T> {
    Collection<T> findAll();
    Object getId(T node);
    Object getParentId(T node);
    String getCaption(T node);
    Collection<T> find(Object searchSpecification);
}
