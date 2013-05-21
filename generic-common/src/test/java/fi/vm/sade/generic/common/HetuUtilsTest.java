package fi.vm.sade.generic.common;

import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class HetuUtilsTest {
    private final Logger log = LoggerFactory.getLogger(HetuUtilsTest.class);

    @Test
    public void testGenerateHetu() {
        for (int count = 0; count < 50; count++) {
            final String hetu = HetuUtils.generateHetu();
            log.info("Generated hetu [{}], validating...", hetu);
            Assert.assertTrue(String.format("Hetu [%s] should be valid", hetu), HetuUtils.isHetuValid(hetu));
        }
    }

    @Test
    public void testValidatorWithInvalidHetu() {
        Assert.assertFalse("Hetu [010170-001A] should be invalid", HetuUtils.isHetuValid("010170-001A"));
        Assert.assertFalse("Hetu [-1] should be invalid", HetuUtils.isHetuValid("-1"));
        Assert.assertFalse("Hetu [999999B0000] should be invalid", HetuUtils.isHetuValid("999999B0000"));
    }

    @Test
    public void testMaleHetuValidator() {
        Assert.assertTrue("Hetu [181172-3434] should be male", HetuUtils.isMaleHetu("181172-3434"));
    }

    @Test
    public void testFemaleHetuValidator() {
        Assert.assertTrue("Hetu [280583-800A] should be female", HetuUtils.isFemaleHetu("280583-800A"));
    }

    /*
    @Test
    public void testGenerateHetusWithArgs() {
        final String sukupuoliListaus = "Mies\n" +
                "Mies\n" +
                "Nainen\n" +
                "Mies\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Mies\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Mies\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Mies\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Mies\n" +
                "Mies\n" +
                "Nainen\n" +
                "Mies\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Mies\n" +
                "Mies\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Mies\n" +
                "Mies\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Mies\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Mies\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Mies\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Mies\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Mies\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Mies\n" +
                "Nainen\n" +
                "Mies\n" +
                "Mies\n" +
                "Nainen\n" +
                "Mies\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Mies\n" +
                "Nainen\n" +
                "Mies\n" +
                "Mies\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Mies\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Mies\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Mies\n" +
                "Nainen\n" +
                "Nainen\n" +
                "Mies\n" +
                "Nainen";
        final String[] sukupuolet = StringUtils.split(sukupuoliListaus, '\n');
        final Set<String> generated = new HashSet<String>();
        final Random random = new Random();
        for (int index = 0; index < sukupuolet.length; index++) {
            String hetu = HetuUtils.generateHetuWithArgs(1, 1, 1901, sukupuolet[index] == "Mies" ? 1 : 0);
            while (generated.contains(hetu)) {
                hetu = HetuUtils.generateHetuWithArgs(1, 1, 1901, sukupuolet[index] == "Mies" ? 1 : 0);
            }
            generated.add(hetu);
            System.out.println(hetu);
        }
    }
    */
}
