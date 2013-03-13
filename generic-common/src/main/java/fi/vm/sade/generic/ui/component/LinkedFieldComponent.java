/**
 *
 */
package fi.vm.sade.generic.ui.component;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * @author tommiha
 */
@SuppressWarnings("serial")
public class LinkedFieldComponent extends VerticalLayout {
    private AbstractField primaryField;
    private Map<AbstractField, Label> otherFields;

    private CheckBox linked;

    private boolean readOnly = false;
    private String linkedText;
    private Label primaryLabel;
    private Label titleLabel;

    private void addField(GridLayout fieldLayout, Label label, AbstractField field) {
        int row = fieldLayout.getRows();
        fieldLayout.insertRow(row);

        fieldLayout.addComponent(field, 0, row);
        fieldLayout.setComponentAlignment(field, Alignment.TOP_LEFT);

        fieldLayout.addComponent(label, 1, row);
        fieldLayout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
        label.setSizeUndefined();

        field.setWidth("100%");
    }

    public LinkedFieldComponent(String linkedText, AbstractField... fields) {
        this.linkedText = linkedText;
        if(fields == null || fields.length == 0) {
            throw new RuntimeException("Linked fields are not given.");
        }
        this.primaryField = fields[0];
        this.primaryLabel = extractCaptions(fields[0]);
        this.otherFields = new HashMap<AbstractField, Label>();
        for(int i = 0; i < fields.length; i++) {
            if(i == 0) {
                continue;
            }
            AbstractField field = fields[i];
            otherFields.put(field, extractCaptions(field));
        }
        this.initializeComponent();
    }

    private Label extractCaptions(AbstractField field) {
        String caption = field.getCaption();
        field.setCaption(null);
        return new Label(caption);
    }

    public LinkedFieldComponent(String linkedText, AbstractField primaryField, Label primaryFieldLabel,
            Map<AbstractField, Label> otherFields) {
        this.linkedText = linkedText;
        this.primaryField = primaryField;
        this.primaryLabel = primaryFieldLabel;
        this.otherFields = otherFields;
        initializeComponent();
    }

    private void initializeComponent() {
        setMargin(false, false, true, false);
        setWidth("100%");

        GridLayout fieldLayout = new GridLayout(2, 1);
        fieldLayout.setWidth("100%");
        fieldLayout.setColumnExpandRatio(0, 1.0f);
        fieldLayout.setSpacing(true);

        titleLabel = new Label();
        fieldLayout.addComponent(titleLabel, 0, 0);

        linked = new CheckBox(linkedText);
        linked.addListener(new LinkedCheckBoxValueChangeListener());
        linked.setImmediate(true);
        fieldLayout.addComponent(linked, 1, 0);
        fieldLayout.setComponentAlignment(linked, Alignment.BOTTOM_RIGHT);

        addField(fieldLayout, this.primaryLabel, primaryField);

        primaryField.setImmediate(true);

        if (primaryField instanceof AbstractTextField) {
            ((AbstractTextField) primaryField).setTextChangeEventMode(TextChangeEventMode.EAGER);
            ((AbstractTextField) primaryField).addListener((TextChangeListener) new PrimaryFieldValueChangeListener());
        } else {
            primaryField.addListener(new PrimaryFieldValueChangeListener());
        }

        if (primaryField instanceof AbstractField) {
            ((AbstractField) primaryField).setImmediate(true);
        }

        for (Entry<AbstractField, Label> e : otherFields.entrySet()) {
            addField(fieldLayout, e.getValue(), e.getKey());
        }

        addComponent(fieldLayout);
    }

    private class LinkedCheckBoxValueChangeListener implements ValueChangeListener {

        @Override
        public void valueChange(ValueChangeEvent event) {
            for (Entry<AbstractField, Label> e : otherFields.entrySet()) {
                if (linked.booleanValue()) {
                    e.getKey().setValue(primaryField.getValue());
                }
                e.getKey().setEnabled(!linked.booleanValue());
                e.getValue().setEnabled(!linked.booleanValue());
            }
        }
    }

    private class PrimaryFieldValueChangeListener implements ValueChangeListener, TextChangeListener {

        private void setValues(Object newValue) {
            if (linked.booleanValue()) {
                for (Entry<AbstractField, Label> e : otherFields.entrySet()) {
                    e.getKey().setValue(newValue);
                }
            }
        }

        @Override
        public void valueChange(ValueChangeEvent event) {
            setValues(primaryField.getValue());
        }

        @Override
        public void textChange(TextChangeEvent event) {
            setValues(event.getText());
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;

        primaryField.setReadOnly(readOnly);
        for (Entry<AbstractField, Label> e : otherFields.entrySet()) {
            e.getKey().setReadOnly(readOnly);
        }

        linked.setVisible(!readOnly);
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    public void setTitle(String title) {
        this.titleLabel.setContentMode(Label.CONTENT_DEFAULT);
        this.titleLabel.setPropertyDataSource(new ObjectProperty<String>(title, String.class));
    }
}
