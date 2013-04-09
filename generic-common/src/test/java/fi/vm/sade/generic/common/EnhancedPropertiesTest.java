package fi.vm.sade.generic.common;

import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;

/**
 * @author Antti Salonen
 */
public class EnhancedPropertiesTest {

    @Test
    public void testPropertySubstitution() throws IOException {
        Properties p = new EnhancedProperties();
        p.load(new StringReader("" +
                "foo=bar\n" +
                "asd=x${foo}\n" +
                "asd2=${foo}${asd}\n"+
                "asd3=_${asd2}_\n"+
        ""));
        assertEquals("xbar", p.getProperty("asd"));
        assertEquals("barxbar", p.getProperty("asd2"));
        assertEquals("_barxbar_", p.getProperty("asd3"));
    }

}
