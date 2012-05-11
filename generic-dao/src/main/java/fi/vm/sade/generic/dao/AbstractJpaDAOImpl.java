/**
 *
 */
package fi.vm.sade.generic.dao;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * Abstract implementation of JpaDAO.
 *
 * @author tommiha
 */
public abstract class AbstractJpaDAOImpl<E, ID> implements JpaDAO<E, ID> {

    private EntityManager entityManager;
    private Class<E> entityClass;

    @SuppressWarnings("unchecked")
    public AbstractJpaDAOImpl() {
        entityClass = (Class<E>) ((ParameterizedType) (getClass().getGenericSuperclass())).getActualTypeArguments()[0];
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /*
     * (non-Javadoc)
     * @see fi.vm.sade.jdbc.dao.JpaDAO#read(java.io.Serializable)
     */
    public E read(ID key) {
        return entityManager.find(entityClass, key);
    }

    /*
     * (non-Javadoc)
     * @see fi.vm.sade.jdbc.dao.JpaDAO#update(java.io.Serializable)
     */
    public void update(E entity) {
        entityManager.merge(entity);
        entityManager.flush();
    }

    /*
     * (non-Javadoc)
     * @see fi.vm.sade.jdbc.dao.JpaDAO#insert(java.io.Serializable)
     */
    public E insert(E entity) {
        entityManager.persist(entity);
        // Database must be synchronized at this point or the insert query does not get executed at all
        entityManager.flush();
        return entity;
    }

    /*
    * (non-Javadoc)
    * @see fi.vm.sade.jdbc.dao.JpaDAO#remove(java.io.Serializable)
    */
    public void remove(E entity) {
        entityManager.remove(entity);
    }

    /*
     * (non-Javadoc)
     * @see fi.vm.sade.jdbc.dao.JpaDAO#findAll
     */
    public List<E> findAll() {
        Query query = getEntityManager().createQuery("SELECT x FROM " + entityClass.getSimpleName() + " x");
        return query.getResultList();
    }

    public List<E> findBy(String column, Object value) {
        Query query = getEntityManager().createQuery("SELECT x FROM " + entityClass.getSimpleName() + " x WHERE x."+column+" = :value");
        query.setParameter("value", value);
        return query.getResultList();
    }


}
