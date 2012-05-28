/**
 * 
 */
package fi.vm.sade.selenium.aspect;

import com.vaadin.Application;

/**
 * @author tommiha
 *
 */
public interface ApplicationListener<T extends Application> {

    void onApplicationConstruct(Application application);
}
