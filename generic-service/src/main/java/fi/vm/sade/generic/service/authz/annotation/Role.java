package fi.vm.sade.generic.service.authz.annotation;

/**
 * Created with IntelliJ IDEA.
 * User: Eetu Blomqvist
 * Date: 24.8.2012
 * Time: 16:37
 * To change this template use File | Settings | File Templates.
 */
public enum Role {

    READ, READ_UPDATE, CRUD;

    public static Role fromValue(String value){
        for (Role role : values()) {
            if(role.name().equalsIgnoreCase(value)){
                return role;
            }
        }
        return null;
    }
}
