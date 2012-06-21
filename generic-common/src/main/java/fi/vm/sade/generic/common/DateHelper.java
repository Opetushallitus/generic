package fi.vm.sade.generic.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public final class DateHelper {

    private static String DEFAULT_DATE_FORMAT_STRING = "dd-MM-yy HH:mm";
    private static SimpleDateFormat formatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT_STRING);

    public static String xmlCalToTypicalString(XMLGregorianCalendar cal) {
        if (cal != null) {
            Date date = xmlCalToDate(cal);
            return dateToTypicalString(date);
        }
        return "";
    }

    public static String getDefaultDateFormatString() {
        return DEFAULT_DATE_FORMAT_STRING;
    }

    public static String dateToTypicalString(Date date) {
        if (date != null) {
            return formatter.format(date);
        }
        return "";
    }

    public static Date xmlCalToDate(XMLGregorianCalendar cal) {
        if (cal != null) {
            return cal.toGregorianCalendar().getTime();
        }
        return null;
    }

    public static XMLGregorianCalendar DateToXmlCal(Date date) {
        try {
            GregorianCalendar gcal = new GregorianCalendar();
            gcal.setTime(date);
            XMLGregorianCalendar xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
            return xgcal;
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException("Operation with XMLGregorianCalendar failed", e);
        }
    }

}
