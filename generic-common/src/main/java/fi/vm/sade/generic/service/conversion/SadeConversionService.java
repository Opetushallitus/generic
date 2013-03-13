package fi.vm.sade.generic.service.conversion;

import java.util.List;
import java.util.Set;

import org.springframework.core.convert.ConversionService;

public interface SadeConversionService extends ConversionService {

    /**
     * Convert the source collection to List of targetType.
     * 
     * @param source
     *            the source object to convert (may be null)
     * @param targetType
     *            the target type to convert to (required)
     * @return the converted object, an instance of targetType
     *             if an exception occurred
     */
    <T, E extends List<?>> List<T> convertAll(E source, Class<T> targetType);

    <T, E extends Set<?>> Set<T> convertAll(E source, Class<T> targetType);
}
