package fi.vm.sade.security.xssfilter;

import javax.persistence.EntityListeners;
import javax.persistence.PreUpdate;

import javax.persistence.PrePersist;

/**
 * Hibernate-kuuntelijatoteutus xss-filtterille.
 * 
 * @see EntityListeners
 * @see XssFilter
 * @see FilterXss
 * @author Timo Santasalo / Teknokala Ky
 */
public class XssFilterListener {

	@PrePersist
	@PreUpdate
	public void filter(Object entity) {
		XssFilter.filterAll(entity);
	}
	
}
