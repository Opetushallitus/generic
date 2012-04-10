package fi.vm.sade.generic.common;

import java.io.Serializable;

/**
 * @author Antti Salonen
 */
public class BaseDTO implements Serializable {
    
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
