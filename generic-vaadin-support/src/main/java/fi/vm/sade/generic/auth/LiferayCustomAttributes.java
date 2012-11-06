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
package fi.vm.sade.generic.auth;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.model.Organization;
import com.liferay.portal.model.User;
import com.liferay.portal.service.ClassNameLocalServiceUtil;
import com.liferay.portlet.expando.model.ExpandoTable;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.model.ExpandoValue;
import com.liferay.portlet.expando.service.ExpandoColumnLocalServiceUtil;
import com.liferay.portlet.expando.service.ExpandoTableLocalServiceUtil;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;

public class LiferayCustomAttributes {
    private static final Log log = LogFactoryUtil.getLog(LiferayCustomAttributes.class);

    public static final String OID_HENKILO = "oid_henkilo";

    public static final String IDP_ENTITY_ID = "idp_entity_id";
    public static final String IDENTIFIER = "identifier";

    public static final String STRONGLY_AUTHENTICATED = "strongly_authenticated";

    public static final String ORGANISAATIO_OID = "organisaatio_oid";

    public static final String DEFAULT_USER_ROLE_NAME="User";

//    public static final String DEFAULT_SITE_NAME="Guest"; //this is the "Liferay" group
    public static final String DEFAULT_SITE_NAME="Virkailijan ty\u00f6p\u00f6yt\u00e4"; //this is the "Liferay" group

    /**
     * Set custom attribute to liferay user
     *
     * @param userId
     * @param companyId
     * @param attribute
     * @param value
     * @return
     */
    public static String setCustomAttributeToUser(Long userId, Long companyId, String attribute, String value) {
        String output = "";
        try {
            ExpandoValueLocalServiceUtil.addValue(companyId, User.class.getName(),
                    ExpandoTableConstants.DEFAULT_TABLE_NAME, attribute, userId, value);
        } catch (Exception e) {
            log.warn("Couldn't set custom user attribute: " + attribute + " : " + value);
        }

        return output;
    }

    /**
     * Set custom attribute to liferay user
     *
     * @param userId
     * @param companyId
     * @param attribute
     * @param value
     * @return
     */
    public static void setCustomAttributeToOrganization(Long organizationId, Long companyId, String attribute,
            String value) {
        try {
            ExpandoValueLocalServiceUtil.addValue(companyId, Organization.class.getName(),
                    ExpandoTableConstants.DEFAULT_TABLE_NAME, attribute, organizationId, value);
        } catch (Exception e) {
            log.warn("Couldn't set custom user attribute: " + attribute + " : " + value);
        }

    }

    public static String getCustomAttributeFromUser(Long userId, Long companyId, String attribute) {
        String output = "";
        try {
            ExpandoValue value = ExpandoValueLocalServiceUtil.getValue(companyId, User.class.getName(),
                    ExpandoTableConstants.DEFAULT_TABLE_NAME, attribute, userId);
            if (value != null)
                output = (String) value.getString();
        } catch (Exception e) {
            log.warn("Couldn't get custom user attribute: " + attribute);
        }

        return output;
    }

    /**
     * Add custom attribute to liferay.
     *
     * @param companyId
     * @param className
     * @param columnName
     * @param columnType
     * @throws com.liferay.portal.kernel.exception.PortalException
     * @throws com.liferay.portal.kernel.exception.SystemException
     */
    public static void addCustomColumn(long companyId, String className, String columnName, int columnType)
            throws PortalException, SystemException {
        long classNameId = ClassNameLocalServiceUtil.getClassNameId(className);
        ExpandoTable table;
        try {
            table = ExpandoTableLocalServiceUtil.getDefaultTable(companyId, classNameId);
        } catch (Exception e) {
            table = ExpandoTableLocalServiceUtil.addDefaultTable(companyId, classNameId);
        }

        if (ExpandoColumnLocalServiceUtil.getColumn(table.getTableId(), columnName) == null) {
            ExpandoColumnLocalServiceUtil.addColumn(table.getTableId(), columnName, columnType);
        }
    }
}
