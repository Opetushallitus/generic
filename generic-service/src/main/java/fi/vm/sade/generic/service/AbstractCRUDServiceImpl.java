/**
 *
 */
package fi.vm.sade.generic.service;

import fi.vm.sade.generic.common.ValidationException;
import fi.vm.sade.generic.dao.JpaDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author tommiha
 */
@Transactional
public abstract class AbstractCRUDServiceImpl<DTOCLASS, FATDTOCLASS, JPACLASS, IDCLASS> implements CRUDService<JPACLASS, IDCLASS> {

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
    protected FATDTOCLASS read(IDCLASS key) {
        log.debug("Reading record by primary key: " + key);
        return convertToFatDTO(dao.read(key));
    }

    /*
     * (non-Javadoc)
     * @see fi.vm.sade.generic.service.CRUDService#update(java.lang.Object)
     */
    protected void update(DTOCLASS dto) throws ValidationException {
        validateDTO(dto);
        JPACLASS entity = convertToJPA(dto, true);
        updateJPA(entity);
    }

    protected void updateJPA(JPACLASS entity) throws ValidationException {
        if (entity == null) {
            throw new RuntimeException("Entity is null.");
        }
        log.debug("Updating record: " + entity);
        validateJPA(entity);
        dao.update(entity);
    }

    /*
     * (non-Javadoc)
     * @see fi.vm.sade.generic.service.CRUDService#insert(java.lang.Object)
     */
    protected DTOCLASS insert(DTOCLASS dto) throws ValidationException {
        validateDTO(dto);
        JPACLASS entity = convertToJPA(dto, true);
        entity = insertJPA(entity);
        return convertToDTO(entity);
    }

    protected JPACLASS insertJPA(JPACLASS entity) throws ValidationException {
        log.debug("Inserting record: " + entity);

        try {

            validateJPA(entity);
            entity = dao.insert(entity);
            return entity;

        } catch (PersistenceException e) {
            // TODO: unique kenttien validointi etukäteen? myös updateen?
            if (e.getCause().toString().contains("ConstraintViolationException")) {
                throw new ValidationException("constraint violation! unique problem, row with same value already exists?", e);
            }
            throw e;
        }

    }

    /*
     * (non-Javadoc)
     * @see fi.vm.sade.generic.service.CRUDService#delete(java.lang.Object)
     */
    protected void delete(DTOCLASS dto) {
        JPACLASS entity = convertToJPA(dto, true);
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

    protected List<DTOCLASS> findAll() {
        return convertToDTO(dao.findAll());
    }

    protected abstract FATDTOCLASS convertToFatDTO(JPACLASS entity);

    protected abstract DTOCLASS convertToDTO(JPACLASS entity);

    protected abstract JPACLASS convertToJPA(DTOCLASS dto, boolean merge);

    protected List<DTOCLASS> convertToDTO(Collection<JPACLASS> entitys) {
        if (entitys == null) {
            return null;
        }
        List<DTOCLASS> result = new ArrayList<DTOCLASS>();
        for (JPACLASS entity : entitys) {
            result.add(convertToDTO(entity));
        }
        return result;
    }

    protected List<JPACLASS> convertToJPA(Collection<DTOCLASS> dtos, boolean merge) {
        if (dtos == null) {
            return null;
        }
        List<JPACLASS> result = new ArrayList<JPACLASS>();
        for (DTOCLASS dto : dtos) {
            result.add(convertToJPA(dto, merge));
        }
        return result;
    }

    /**
     * validate based on JSR-303 annoations
     */
    protected void validateDTO(DTOCLASS dto) throws ValidationException {
        validate(dto);
    }

    /**
     * validate based on JSR-303 annoations
     */
    protected void validateJPA(JPACLASS entity) throws ValidationException {
        validate(entity);
    }

    /**
     * validate based on JSR-303 annoations
     */
    protected void validate(Object dtoOrEntity) throws ValidationException {
        Validator validator = ValidatorFactoryBean.getValidator();
        Set<ConstraintViolation<Object>> validationResult = validator.validate(dtoOrEntity);
        log.debug("validate, validator: "+validator + ", validationResult: " + validationResult);
        if (validationResult.size() > 0) {
            ValidationException validationException = new ValidationException(validationResult);
            for (ConstraintViolation<Object> violation : validationResult) {
                validationException.addValidationMessage(violation.getPropertyPath() + " - " + violation.getMessage()
                        + " (was: " + violation.getInvalidValue() + ")");
            }
            throw validationException;
        }
    }

    protected void validateProperty(Object dtoOrEntity, String propertyName) throws ValidationException {
        Validator validator = ValidatorFactoryBean.getValidator();
        Set<ConstraintViolation<Object>> validationResult = validator.validateProperty(dtoOrEntity, propertyName);
        log.debug("validate, validator: "+validator + ", validationResult: " + validationResult);
        if (validationResult.size() > 0) {
            ValidationException validationException = new ValidationException();
            for (ConstraintViolation<Object> violation : validationResult) {
                validationException.addValidationMessage(violation.getPropertyPath() + " - " + violation.getMessage()
                        + " (was: " + violation.getInvalidValue() + ")");
            }
            throw validationException;
        }
    }

}
