package fi.vm.sade.generic.common.auth.annotation;

import fi.vm.sade.generic.common.auth.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Eetu Blomqvist
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Deprecated // TODO:cas todo poista?
public @interface RequiresRole {

    Role[] roles();
}

