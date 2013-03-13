package fi.vm.sade.generic.service.conversion;

import org.springframework.core.convert.converter.Converter;

import fi.vm.sade.generic.model.BaseEntity;

public abstract class AbstractFromDomainConverter<FROM extends BaseEntity, TO> implements Converter<FROM, TO> {

}
