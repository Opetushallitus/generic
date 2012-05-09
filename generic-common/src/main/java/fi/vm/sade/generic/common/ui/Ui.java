package fi.vm.sade.generic.common.ui;

import java.lang.annotation.ElementType;

/**
 * Annotate field (or class) with information on how they should be handled when creating user interface for them.
 * GuiFactory -class reads these annotations when creatinbg user interface components.
 *
 * @see fi.vm.sade.organisaatio.ui.sandbox.GuiFactory
 * @author Antti Salonen
 */
@Deprecated
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.FIELD, ElementType.TYPE})
public @interface Ui {
    /**
     * true if field is complex object, and we want to create a nested form for it when editing 
     */
    boolean inlineEdit() default false;
    /**
     * Options: (auto), text, textarea, select, combo_suggesting_contains
     */
    String editType() default "text";
    /**
     * Hint text when loading options. Can be used if same OptionsProvider/GuiHelper is used in many places.   
     */
    String optionsHint() default "none";
    /**
     * Profiles are handy when we need to view/edit the target differently in different views/situations. 
     * Multiple Ui annotations of different profile can be set using Uis-annotation.
     */
    String profile() default "default";
    /**
     * true if the field should not be viewed/edited by user interface
     */
    boolean disabled() default false;
    /**
     * list if disabled fields, meaningful only in class-level
     */
    String[] disabledFields() default {};
    /**
     * Mitä kohdeluokan getteriä käyttäen renderöidään kentän arvo näytettäessä tai valitessa - TODO: valinta kesken?, näyttö tekemättä
     */
    String itemProperty() default "";
    /**
     * For relations if field is just id (eg. Long parentId)
     */
    Class targetClass() default Object.class;
    /**
     * Together with targetClass
     */
    String targetProperty() default "";
    /**
     * Set field caption or no
     */
    boolean caption() default true;
    /**
     * Load options once when creating the component (false) or dynamically when onfocus (true)
     */
    boolean dynamic() default false;
}
