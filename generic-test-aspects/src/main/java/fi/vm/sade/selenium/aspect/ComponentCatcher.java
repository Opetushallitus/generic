/**
 * 
 */
package fi.vm.sade.selenium.aspect;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.Application;
import com.vaadin.ui.Component;

/**
 * @author tommiha
 *
 */
public class ComponentCatcher {

    private final Map<Class<Component>, ComponentListener<Component>> listeners = 
            new HashMap<Class<Component>, ComponentListener<Component>>();
    
    private final Map<Class<Application>, ApplicationListener<Application>> appListeners = 
            new HashMap<Class<Application>, ApplicationListener<Application>>();
    
    @SuppressWarnings("unchecked")
    public void registerListener(Class<? extends Component> componentClass, ComponentListener<? extends Component> listener) {
        this.listeners.put((Class<Component>)componentClass, (ComponentListener<Component>)listener);
    }
    
    @SuppressWarnings("unchecked")
    public void registerListener(Class<? extends Application> applicationClass, ApplicationListener<? extends Application> listener) {
        this.appListeners.put((Class<Application>)applicationClass, (ApplicationListener<Application>)listener);
    }
    
    public void execute(Component component) {
        if(component == null) {
            return;
        }
        for(Class<? extends Component> componentClass : listeners.keySet()) {
            if(componentClass.isAssignableFrom(component.getClass())) {
                ComponentListener<Component> listener = listeners.get(componentClass);
                listener.onComponentConstruct(component);
            }
        }
    }
    
    public void execute(Application application) {
        if(application == null) {
            return;
        }
        for(Class<? extends Application> componentClass : appListeners.keySet()) {
            if(componentClass.isAssignableFrom(application.getClass())) {
                ApplicationListener<Application> listener = appListeners.get(componentClass);
                listener.onApplicationConstruct(application);
            }
        }
    }
}
