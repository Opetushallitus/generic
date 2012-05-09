package fi.vm.sade.generic.common.ui;

import java.lang.annotation.ElementType;

/**
 * Collection of Ui-annotations, allows adding multiple Ui-annotations for target,
 * useful when when need different Ui definitions for different 'profile's.
 *
 * @see Ui
 * @author Antti Salonen
 */
@Deprecated
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.FIELD, ElementType.TYPE})
public @interface Uis {
    Ui[] uis();
}
