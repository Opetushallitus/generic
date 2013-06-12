/*
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
package fi.vm.sade.security.xssfilter;

import org.junit.Assert;
import org.junit.Test;

import fi.vm.sade.security.xssfilter.XssFilter;

public class XssFilterTest extends Assert {
	
	private void testFilter(String expected, String input) {
		assertEquals(expected, XssFilter.filter(input));
	}

	@Test
	public void testXss() {
		
		testFilter(null, null);
		testFilter("foo", "foo");
		testFilter("f<i>o</i>o", "f<i>o</i>o");
		testFilter("is  this", "is <script>evil()</script> this");
		testFilter("is  this", "is <style>evil()</style> this");
		
	}

}
