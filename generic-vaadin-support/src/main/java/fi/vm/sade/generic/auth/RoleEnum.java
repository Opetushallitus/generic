/**
 * 
 */
package fi.vm.sade.generic.auth;

/**
 * @author hannu
 *
 */
public enum RoleEnum {

	NONE("none"),
	APP_KOODISTO_R("APP_KOODISTO_R"),
	APP_KOODISTO_RU("APP_KOODISTO_RU"),
	APP_KOODISTO_CRUD("APP_KOODISTO_CRUD");
	

	private String value;

	RoleEnum(String code) {
		this.value = code;
	}

	public String getValue() {
		return this.value;
	}

}
