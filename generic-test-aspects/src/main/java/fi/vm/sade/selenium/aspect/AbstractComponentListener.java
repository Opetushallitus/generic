/**
 * 
 */
package fi.vm.sade.selenium.aspect;

import com.vaadin.ui.Component;

/**
 * @author tommiha
 *
 */
public abstract class AbstractComponentListener<T extends Component> implements ComponentListener<T> {

    /* (non-Javadoc)
     * @see fi.vm.sade.koodisto.aspect.ComponentListener#onComponentConstruct(com.vaadin.ui.Component)
     */
    @SuppressWarnings("unchecked")
    @Override
    public final void onComponentConstruct(Component component) {
        onComponent((T) component);
    }

    public abstract void onComponent(T component);
}
