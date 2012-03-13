/**
 * 
 */
package fi.vm.sade.dbunit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import fi.vm.sade.dbunit.listener.CleanInsertTestExecutionListener;

/**
 * Defines where to find DBUnit data set file. {@link CleanInsertTestExecutionListener} 
 * uses this annotation to populate the database.
 * 
 * @author tommiha
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
public @interface DataSetLocation {
	public String value();
}
