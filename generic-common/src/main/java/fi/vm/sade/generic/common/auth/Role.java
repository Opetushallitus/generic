package fi.vm.sade.generic.common.auth;

/**
 * Enumeration for role names.
 *
 * @author Eetu Blomqvist
 */
public enum Role {

    READ, READ_UPDATE, CRUD, NOT_REQUIRED;

    public static Role fromValue(String value) {
        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        return null;
    }

    public String toString(){
        return name();
    }
}
