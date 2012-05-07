package fi.vm.sade.generic.dao;

import java.util.List;

import fi.vm.sade.generic.model.BaseEntity;

public interface GenericDAO {
    /**
     * Reads a single record from the database
     * 
     * @param entity
     * @param key
     * @return
     */
    <E extends BaseEntity> E read(Class<E> entity, Long key);

    /**
     * Updates an existing record
     * 
     * @param entity
     */
    <E extends BaseEntity> void update(E entity);

    /**
     * Creates a new record to the database
     * 
     * @param entity
     * @return
     */
    <E extends BaseEntity> E insert(E entity);

    /**
     * Lists all objects of given type from database
     * 
     * @param entity
     * @return
     */
    <E extends BaseEntity> List<E> findAll(Class<E> entity);

    /**
     * Lists all objects of given type with the matching field value from
     * database
     * 
     * @param entity
     * @param column
     * @param value
     * @return
     */
    <E extends BaseEntity> List<E> findBy(Class<E> entity, String column, Object value);
}
