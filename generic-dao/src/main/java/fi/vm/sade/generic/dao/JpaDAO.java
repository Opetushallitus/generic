/**
 *
 */
package fi.vm.sade.generic.dao;


/**
 * Interface of generic DAO.
 *
 * @author tommiha
 *
 */
public interface JpaDAO<E, ID> {

	/**
	 * Reads single record from the database.
	 * @param key
	 * @return
	 */
	E read(ID key);

	/**
	 * Updates an existing record.
	 * @param entity
	 */
	void update(E entity);

	/**
	 * Creates a new record to the database.
	 * @param entity
	 */
	void insert(E entity);

	/**
	 * Removes existing record from the database.
	 * @param entity
	 */
	void remove(E entity);
}
