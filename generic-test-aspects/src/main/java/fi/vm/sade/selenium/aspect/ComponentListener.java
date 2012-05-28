/**
 * 
 */
package fi.vm.sade.selenium.aspect;

import com.vaadin.ui.Component;

/**
 * @author tommiha
 *
 */
public interface ComponentListener<T extends Component> {

    void onComponentConstruct(Component component);
}
