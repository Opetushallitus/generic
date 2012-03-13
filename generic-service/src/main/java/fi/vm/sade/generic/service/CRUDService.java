/**
 * 
 */
package fi.vm.sade.generic.service;

/**
 * @author tommiha
 *
 */
public interface CRUDService<E, ID> {

	/**
	 * Reads single record from the database.
	 * @param key
	 * @return
	 */
	public abstract E read(ID key);

	/**
	 * Updates an existing record.
	 * @param entity
	 */
	public abstract void update(E entity);

	/**
	 * Creates a new record to the database.
	 * @param entity
	 */
	public abstract void insert(E entity);

	/**
	 * Removes existing record from the database.
	 * @param entity
	 */
	public abstract void delete(E entity);

	/**
	 * Removes existing record from the database with ID.
	 * @param entity
	 */
	public abstract void deleteById(ID id);
}
