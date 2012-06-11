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

package fi.vm.sade.generic.common.validation;

import java.util.Locale;

/**
 * @author Antti Salonen
 */
public interface MultiLingualText {

    public String PROPERTY_TEXT_FI = "textFi";
    public String PROPERTY_TEXT_SV = "textSv";
    public String PROPERTY_TEXT_EN = "textEn";

    String getTextFi();
    void setTextFi(String textFi);
    String getTextSv();
    void setTextSv(String textSv);
    String getTextEn();
    void setTextEn(String textEn);
    String getClosest(Locale locale);
    boolean allAreNull();
}
