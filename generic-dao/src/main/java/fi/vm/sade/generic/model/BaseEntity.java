/**
 *
 */
package fi.vm.sade.generic.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author tommiha
 */
@MappedSuperclass
@XmlRootElement(name = "BaseEntity")
@XmlAccessorType(XmlAccessType.FIELD)
public class BaseEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1482830143396044915L;

    @Id
    @Column(name = "ID", unique = true, nullable = false)
    @GeneratedValue
    @XmlElement
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;

  
        }
        // XXX: eblomqvist: removed - direct dependency to Hibernate
//        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
//            return false;
//        }

        return id != null && id.equals(((BaseEntity) o).getId());
    }

    @Override
    public int hashCode() {
        return id == null ? super.hashCode() : id.hashCode();
    }
}
