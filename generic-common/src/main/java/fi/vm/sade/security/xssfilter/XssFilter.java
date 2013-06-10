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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;

import com.google.common.base.Preconditions;

/**
 * AntiSamy-pohjainen XSS-filtteri; vaatii profiilitiedoston, jonka oletetaan löytyvän classpathista nimellä
 * "fi.vm.sade.antisamy.xml".
 * 
 * @author Timo Santasalo / Teknokala Ky
 */
public final class XssFilter {
	
	private static final String ANTISAMY_POLICY = "fi.vm.sade.antisamy.xml";
	private static final AntiSamy antiSamy;
	
	static {
		try {
			antiSamy = new AntiSamy(Policy.getInstance(
				Thread.currentThread().getContextClassLoader().getResource(ANTISAMY_POLICY)));
		} catch (PolicyException e) {
			throw new IllegalStateException("Failed to initialized AntiSamy",e);
		}
	}

	private XssFilter() {}

	/**
	 * Filtteröi stringin xss-turvalliseksi.
	 */
	public static String filter(String input) {
		try {
			return input==null ? null : antiSamy.scan(input.trim()).getCleanHTML();
		} catch (ScanException e) {
			throw new IllegalArgumentException("AntiSamy failed while scanning following html: '"+input+"'");
		} catch (PolicyException e) {
			throw new IllegalArgumentException("AntiSamy failed due to invalid profile");
		}
	}
	
	/**
	 * Filtteröi olion kaikki {@link FilterXss}-annotaatiolla merkityt string-kentät.
	 */
	public static void filterAll(Object o) {
		if (o==null) {
			return;
		}
		try {
			filterAll(o,o.getClass());
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Unable to filter annotated field(s)",e);
		}
	}

	private static void filterAll(Object o, Class<?> cc) throws IllegalAccessException {
		for (Field f : cc.getDeclaredFields()) {

			if ((f.getModifiers() & Modifier.STATIC)!=0 || !f.isAnnotationPresent(FilterXss.class)) {
				continue;
			}
			
			Preconditions.checkArgument(f.getType().equals(String.class), "Unable to filter non-string field: %s",f);
			
			f.setAccessible(true);
			
			if (f.get(o)==null) {
				continue;
			}
			
			f.set(o, filter((String) f.get(o)));
		}
		if (cc.getSuperclass() != Object.class) {
			filterAll(o, cc.getSuperclass());
		}
		
	}
	
	
}
