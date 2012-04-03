/**
 * 
 */
package fi.vm.sade.generic.service;

import fi.vm.sade.generic.common.ValidationException;
import fi.vm.sade.generic.dao.JpaDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author tommiha
 *
 */
@Transactional
public abstract class AbstractCRUDServiceImpl<DTOCLASS, JPACLASS, IDCLASS> implements CRUDService<JPACLASS, IDCLASS> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected JpaDAO<JPACLASS, IDCLASS> dao;

    protected JpaDAO<JPACLASS, IDCLASS> getDao() {
        return dao;
    }

    /*
     * (non-Javadoc)
     * @see fi.vm.sade.generic.service.CRUDService#read(java.lang.Object)
     */
    @Transactional(readOnly = true)
    protected DTOCLASS read(IDCLASS key) {
        log.debug("Reading record by primary key: " + key);
        return convertToDTO(dao.read(key));
    }

    /*
     * (non-Javadoc)
     * @see fi.vm.sade.generic.service.CRUDService#update(java.lang.Object)
     */
    protected void update(DTOCLASS dto) throws ValidationException {
        JPACLASS entity = convertToJPA(dto);
        if (entity == null) {
            throw new RuntimeException("Entity is null.");
        }
        log.debug("Updating record: " + entity);
        validate(entity);
        dao.update(entity);
    }

    /*
     * (non-Javadoc)
     * @see fi.vm.sade.generic.service.CRUDService#insert(java.lang.Object)
     */
    protected DTOCLASS insert(DTOCLASS dto) throws ValidationException {
        JPACLASS entity = convertToJPA(dto);
        log.debug("Inserting record: " + entity);
        validate(entity);
        entity = dao.insert(entity);
        return convertToDTO(entity);
    }

    /*
     * (non-Javadoc)
     * @see fi.vm.sade.generic.service.CRUDService#delete(java.lang.Object)
     */
    protected void delete(DTOCLASS dto) {
        JPACLASS entity = convertToJPA(dto);
        log.debug("Deleting record: " + entity);
        dao.remove(entity);
    }

    /*
     * (non-Javadoc)
     * @see fi.vm.sade.generic.service.CRUDService#deleteById(java.lang.Object)
     */
    protected void deleteById(IDCLASS id) {
        JPACLASS entity = dao.read(id);
        log.debug("Deleting record: " + entity);
        dao.remove(entity);
    }

    protected abstract DTOCLASS convertToDTO(JPACLASS entity);

    protected abstract JPACLASS convertToJPA(DTOCLASS dto);

    protected Collection<DTOCLASS> convertToDTO(Collection<JPACLASS> entitys) {
        if (entitys == null) {
            return null;
        }
        List<DTOCLASS> result = new ArrayList<DTOCLASS>();
        for (JPACLASS entity : entitys) {
            result.add(convertToDTO(entity));
        }
        return result;
    }

    protected Collection<JPACLASS> convertToJPA(Collection<DTOCLASS> dtos) {
        if (dtos == null) {
            return null;
        }
        List<JPACLASS> result = new ArrayList<JPACLASS>();
        for (DTOCLASS dto : dtos) {
            result.add(convertToJPA(dto));
        }
        return result;
    }

    protected void validate(JPACLASS entity) throws ValidationException {
        ValidatorFactory validatorFactory = ValidatorFactoryBean.getInstance();
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<JPACLASS>> validationResult = validator.validate(entity);
        log.debug("validate, validator: "+validator+", validationResult: " + validationResult);
        if (validationResult.size() > 0) {
            ValidationException validationException = new ValidationException();
            for (ConstraintViolation<JPACLASS> violation : validationResult) {
                validationException.addValidationMessage(violation.getMessage());
            }
            throw validationException;
        }
    }


}
