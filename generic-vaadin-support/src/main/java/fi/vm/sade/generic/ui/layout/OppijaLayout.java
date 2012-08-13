package fi.vm.sade.generic.ui.layout;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Styled Oppija layout which contains
 * left column (getter: getLeftArea(), cssclass: container-secondary), and
 * main column (getter: getMainArea(), cssclass: container-main).
 *
 * Add components like this: oppijaLayout.getContentArea().addComponent()
 * DO NOT USE: oppijaLayout.addComponent() - TODO: estä käyttö ja/tai komponenttien lisäykset omilla metodeilla esim addMainComponent?
 *
 * @author Antti Salonen
 */
public class OppijaLayout extends HorizontalLayout {

    private VerticalLayout leftArea = new VerticalLayout();
    private HorizontalLayout contentArea = new HorizontalLayout();

    public OppijaLayout() {
        leftArea.setStyleName("container-secondary"); // TODO: korvaa StyleNames.*
        contentArea.setStyleName("container-main");
        contentArea.addComponent(new Label(" "));
        super.addComponent(leftArea);
        super.addComponent(contentArea);
    }

    public VerticalLayout getLeftArea() {
        return leftArea;
    }

    public HorizontalLayout getContentArea() {
        return contentArea;
    }

}
