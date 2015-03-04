package fi.vm.sade.generic.common;

import junit.framework.Assert;
import static junit.framework.Assert.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class HetuUtilsTest {
    //private final Logger log = LoggerFactory.getLogger(HetuUtilsTest.class);

    @Test
    public void testGenerateHetu() {
        for (int count = 0; count < 1000; count++) {
            final String hetu = HetuUtils.generateHetu();
            //log.info("Generated hetu [{}], validating...", hetu);
            Assert.assertTrue("Identifier should start with 9", hetu.charAt(7) == '9');
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
        final String sukupuoliListaus = "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "MIES\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "MIES\n" +
                "MIES\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "MIES\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "MIES\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "MIES\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "MIES\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "MIES\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "MIES\n" +
                "MIES\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES\n" +
                "MIES\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "NAINEN\n" +
                "MIES";
        final String[] sukupuolet = StringUtils.split(sukupuoliListaus, '\n');
        final Set<String> generated = new HashSet<String>();
        final Random random = new Random();
        for (int index = 0; index < sukupuolet.length; index++) {
            String hetu = HetuUtils.generateHetuWithArgs(1, 1, 1901, StringUtils.equalsIgnoreCase(sukupuolet[index], "Mies") ? 1 : 0);
            while (generated.contains(hetu)) {
                hetu = HetuUtils.generateHetuWithArgs(1, 1, 1901, StringUtils.equalsIgnoreCase(sukupuolet[index], "Mies") ? 1 : 0);
            }
            generated.add(hetu);
            System.out.println(hetu);
        }
    }
    */

    @Test
    public void testMaskHetu() {
        assertEquals("281193*****", HetuUtils.maskHetu("281193-9630"));
    }

    @Test
    public void testMaskNullHetu() {
        assertNull(HetuUtils.maskHetu(null));
    }

    @Test
    public void testMaskShortHetu() {
        assertEquals("121212", HetuUtils.maskHetu("121212"));
    }

    @Test
    public void testMaskFullHetu() {
        assertEquals("***********", HetuUtils.maskHetuFull("281193-9630"));
    }

    @Test
    public void testMaskFullShortHetu() {
        assertEquals("******", HetuUtils.maskHetuFull("281193"));
    }

    @Test
    public void testMaskFullNullHetu() {
        assertEquals(null, HetuUtils.maskHetuFull(null));
    }

    @Test
    public void testMaskUnexpectedLengthHetus() {
        assertEquals("*", HetuUtils.maskHetuFull("A"));
        assertEquals("A", HetuUtils.maskHetu("A"));
        assertEquals("**********************", HetuUtils.maskHetuFull("ABCDEFGHIJKLMNOPQRSTUV"));
        assertEquals("ABCDEF****************", HetuUtils.maskHetu("ABCDEFGHIJKLMNOPQRSTUV"));
    }
}
