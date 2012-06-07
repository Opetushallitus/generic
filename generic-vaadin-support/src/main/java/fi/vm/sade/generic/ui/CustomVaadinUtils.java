package fi.vm.sade.generic.ui;

import java.io.IOException;

import com.vaadin.ui.CustomLayout;

public class CustomVaadinUtils {
    
    public static CustomLayout getCustomLayout(String layoutPath) {
        CustomLayout customLayout = null;
        try {
            customLayout = new CustomLayout(Thread.currentThread().getContextClassLoader().getResourceAsStream(layoutPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return customLayout;
    }

}
