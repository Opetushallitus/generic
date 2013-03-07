package fi.vm.sade.security;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

import java.util.Collection;

/**
 * extends spring LdapUserDetailsMapper to set user's oid into username instead of mail - not necessarily needed, might be able to configure same thing
 *
 * @author Antti Salonen
 */
public class CustomUserDetailsMapper extends LdapUserDetailsMapper {

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
        String oid = ctx.getStringAttribute("employeeNumber");
        if (oid == null) {
            oid = ctx.getStringAttribute("uid");
        }
        UserDetails userDetails = super.mapUserFromContext(ctx, oid, authorities);
        return userDetails;
    }

}
