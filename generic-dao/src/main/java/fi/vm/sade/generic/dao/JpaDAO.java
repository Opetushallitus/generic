/**
 *
 */
package fi.vm.sade.generic.dao;


import java.util.List;

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
    E insert(E entity);

    /**
     * Removes existing record from the database.
     * @param entity
     */
    void remove(E entity);

    /**
     * finds all objects of dao's type from db
     */
    List<E> findAll();

    /**
     * Find.
     *
     * @param column
     * @param value
     * @return
     */
    List<E> findBy(String column, Object value);

    /**
     * Paged find.
     *
     * @param column
     * @param value
     * @param firstResultIndex
     * @param maxResults
     * @return
     */
    List<E> findBy(String column, Object value, int firstResultIndex, int maxResults);
    
    /**
     * Implement custom validation logic when needed.
     * 
     * @param entity 
     */
    void validate(E entity);
}
