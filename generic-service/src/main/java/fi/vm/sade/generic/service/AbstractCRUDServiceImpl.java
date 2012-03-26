/**
 * 
 */
package fi.vm.sade.generic.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.sade.generic.dao.JpaDAO;

/**
 * @author tommiha
 *
 */
@Transactional
public class AbstractCRUDServiceImpl<E, ID> implements CRUDService<E, ID> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private JpaDAO<E, ID> dao;

    public AbstractCRUDServiceImpl(JpaDAO<E, ID> dao) {
        this.dao = dao;
    }

    public JpaDAO<E, ID> getDao() {
        return dao;
    }

    /*
     * (non-Javadoc)
     * @see fi.vm.sade.generic.service.CRUDService#read(java.lang.Object)
     */
    @Transactional(readOnly = true)
    public E read(ID key) {
        log.debug("Reading record by primary key: " + key);
        return dao.read(key);
    }

    /*
     * (non-Javadoc)
     * @see fi.vm.sade.generic.service.CRUDService#update(java.lang.Object)
     */
    public void update(E entity) {
        if (entity == null) {
            throw new RuntimeException("Entity is null.");
        }

        log.debug("Updating record: " + entity);
        dao.update(entity);
    }

    /*
     * (non-Javadoc)
     * @see fi.vm.sade.generic.service.CRUDService#insert(java.lang.Object)
     */
    public void insert(E entity) {
        log.debug("Inserting record: " + entity);
        dao.insert(entity);
    }

    /*
     * (non-Javadoc)
     * @see fi.vm.sade.generic.service.CRUDService#delete(java.lang.Object)
     */
    public void delete(E entity) {
        log.debug("Deleting record: " + entity);
        dao.remove(entity);
    }

    /*
     * (non-Javadoc)
     * @see fi.vm.sade.generic.service.CRUDService#deleteById(java.lang.Object)
     */
    public void deleteById(ID id) {
        E entity = dao.read(id);
        log.debug("Deleting record: " + entity);
        dao.remove(entity);
    }
}
