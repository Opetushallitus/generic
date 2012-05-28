/**
 * 
 */
package fi.vm.sade.selenium.aspect;

import com.vaadin.Application;

/**
 * @author tommiha
 *
 */
public abstract class AbstractApplicationListener<T extends Application> implements ApplicationListener<T> {

    @SuppressWarnings("unchecked")
    @Override
    public void onApplicationConstruct(Application application) {
        onApplication((T) application);
    }

    public abstract void onApplication(T application);
}
