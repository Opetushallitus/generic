/**
 *
 */
package fi.vm.sade.generic.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

/**
 * @author tommiha
 */
@MappedSuperclass
public class BaseEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1482830143396044915L;

    public static final String ID_COLUMN_NAME = "id";
    public static final String VERSION_COLUMN_NAME = "version";

    @Id
    @Column(name = ID_COLUMN_NAME, unique = true, nullable = false)
    @GeneratedValue
    private Long id;

    @Version
    @Column(name = VERSION_COLUMN_NAME, nullable = false)
    private Long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;

        }
        // XXX: eblomqvist: removed - direct dependency to Hibernate
        // if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
        // return false;
        // }

        return o instanceof BaseEntity && id != null && id.equals(((BaseEntity) o).getId());

    }

    @Override
    public int hashCode() {
        return id == null ? super.hashCode() : id.hashCode();
    }
}
