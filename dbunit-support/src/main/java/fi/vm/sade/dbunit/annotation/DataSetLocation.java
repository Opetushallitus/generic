/**
 * 
 */
package fi.vm.sade.dbunit.annotation;

import java.lang.annotation.*;

/**
 * Defines where to find DBUnit data set file. {@link fi.vm.sade.dbunit.listener.CleanInsertTestExecutionListener}
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
    String value();
}
