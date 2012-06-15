/**
 * 
 */
package fi.vm.sade.generic.ui.component;

/**
 * @author tommiha
 *
 */
public interface CaptionFormatter<DTOCLASS> {

    /**
     * Formats displayed caption.
     *
     * @param dto
     * @return
     */
    String formatCaption(DTOCLASS dto);
}
