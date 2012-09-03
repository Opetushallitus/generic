package fi.vm.sade.generic.common;

import fi.vm.sade.generic.common.auth.xml.AuthzDataHolder;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * @author Eetu Blomqvist
 */
public class JAXBUtils {

    public static <T extends Object> T unmarshal(Element element, Class<T> clazz) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement<T> elem = unmarshaller.unmarshal(element, clazz);
        return elem.getValue();
    }


}
