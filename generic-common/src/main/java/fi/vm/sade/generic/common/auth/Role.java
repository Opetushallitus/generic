package fi.vm.sade.generic.common.auth;

/**
 * Enumeration for role names.
 *
 * @author Eetu Blomqvist
 */
@Deprecated // todo: cas todo, rethink, pitäisi saada se applikaatio tuohon mukaan
public enum Role {

    READ, READ_UPDATE, CRUD, OPO, NOT_REQUIRED;

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
