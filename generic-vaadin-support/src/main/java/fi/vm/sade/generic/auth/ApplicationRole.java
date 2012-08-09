package fi.vm.sade.generic.auth;

/**
 * ApplicationRole can be used to store the user's role which holds the highest number
 * of permissions. This role can be used to resolve the content that is available
 * to the user in a given applicaiton.
 * 
 * @author hannu
 *
 */
public class ApplicationRole {

	private static final ThreadLocal <RoleEnum> ROLE = new ThreadLocal<RoleEnum>();
	
	public static void setRole(RoleEnum role) {
		ROLE.set(role);
	}
	
	public static RoleEnum getRole() {
		return ROLE.get();
	}
	
}
