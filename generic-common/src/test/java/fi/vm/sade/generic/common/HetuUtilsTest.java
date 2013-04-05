package fi.vm.sade.generic.common;

import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
}
