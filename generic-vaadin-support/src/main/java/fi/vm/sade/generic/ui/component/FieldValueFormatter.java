/**
 * 
 */
package fi.vm.sade.generic.ui.component;

/**
 * @author tommiha
 *
 */
public interface FieldValueFormatter<DTOCLASS> {

    /**
     * Format field value from dto.
     * @param dto
     * @return
     */
    Object formatFieldValue(DTOCLASS dto);
}
