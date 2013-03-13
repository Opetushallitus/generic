package fi.vm.sade.generic.service.conversion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.core.convert.support.GenericConversionService;


public class SadeConversionServiceImpl extends GenericConversionService implements SadeConversionService {

    @Override
    public <T, E extends List<?>> List<T> convertAll(E source, Class<T> targetType) {
        List<T> list = new ArrayList<T>();
        for (Object e : source) {
            list.add(this.convert(e, targetType));
        }
        return list;
    }

    @Override
    public <T, E extends Set<?>> Set<T> convertAll(E source, Class<T> targetType) {
        Set<T> set = new HashSet<T>();
        for (Object e : source) {
            set.add(this.convert(e, targetType));
        }
        return set;
    }

}
