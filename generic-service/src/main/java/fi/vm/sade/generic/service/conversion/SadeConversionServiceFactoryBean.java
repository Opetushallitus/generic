package fi.vm.sade.generic.service.conversion;

import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.GenericConversionService;

public class SadeConversionServiceFactoryBean extends ConversionServiceFactoryBean {

    @Override
    public Class<? extends ConversionService> getObjectType() {
        return SadeConversionServiceImpl.class;
    }

    @Override
    protected GenericConversionService createConversionService() {
        return new SadeConversionServiceImpl();
    }

}
