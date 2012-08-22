package fi.vm.sade.generic.ui.validation;

import java.util.Collection;


/**
 * @author jukka
 * @version 8/6/1212:47 PM}
 * @since 1.1
 */
public interface ValidatingForm {
    Collection<ValidatingComponent> getValidatingComponents();
}
