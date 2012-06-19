package fi.vm.sade.generic.ui.component;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.customfield.CustomField;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Multiple select that contains a field (usually combobox), add button, and table containing the values.
 * Table also contains remove buttons (property REMOVE_BUTTON) for each row.
 * Note that MultipleSelect can be customized, eg if you need to customize table columns, or change add buttons caption, etc.
 *
 * Note:
 *
 * - set actual field using setField -method
 *
 * @author Antti Salonen
 */
public class MultipleSelectToTableWrapper<FIELDCLASS extends Field> extends CustomField {

    public static final String REMOVE_BUTTON = "removeButton";

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected Class modelClass;
    protected VerticalLayout layout = new VerticalLayout();
    protected HorizontalLayout searchAndAdd;
    protected FIELDCLASS field;
    protected Button addButton = new Button("+", new Button.ClickListener() {
        @Override
        public void buttonClick(Button.ClickEvent clickEvent) {
            Object value = field.getValue();
            if (value != null) {            
                table.addItem(value);
                log.info("added value: " + value + " ("+value.getClass().getSimpleName()+"), values now: " + getValueList());
                table.requestRepaint();
                field.setValue(null);
            }
        }
    });
    protected Table table;
    protected Collection targetCollection;
    /** used when committing values */
    protected FieldValueFormatter fieldValueFormatter = new FieldValueFormatter() {
        @Override
        public Object formatFieldValue(Object dto) {
            return dto;
        }
    };
    /** used when showing values */
    protected FieldValueFormatter fieldValueFormatterReverse = new FieldValueFormatter() {
        @Override
        public Object formatFieldValue(Object dto) {
            return dto;
        }
    };

    public MultipleSelectToTableWrapper(Class modelClass) {
        super();
        this.modelClass = modelClass;
        table = createTable(modelClass);

        // add remove-buttons to each row
        table.addGeneratedColumn(REMOVE_BUTTON, new Table.ColumnGenerator() {
            @Override
            public Object generateCell(Table source, final Object itemId, Object columnId) {
                return new Button("-", new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent clickEvent) {
                        getDataSource().removeItem(itemId);
                        //table.requestRepaint();
                        log.debug("removed value from multiselect, removed: " + itemId + ", values now: " + getValueList());
                    }
                });
            }
        });

        // process layout
        searchAndAdd = new HorizontalLayout();
        layout.addComponent(searchAndAdd);
        searchAndAdd.addComponent(addButton);
        layout.addComponent(table);
        layout.setSizeUndefined();

        //
        setCompositionRoot(layout);
    }

    private <MODELCLASS> Table createTable(Class<MODELCLASS> modelClass) {
        final Table table = new Table();
        Collection initialValues = new ArrayList(); // table alkuun tyhjä, alustetaan setPropertyDataSource:ssa
        final BeanItemContainer<MODELCLASS> tableContainer = new BeanItemContainer<MODELCLASS>(modelClass, initialValues);
        table.setContainerDataSource(tableContainer);
        // wire form to show selected table row
        table.setSelectable(true);
        return table;
    }

    @Override
    public Class<?> getType() {
        return Collection.class;
    }

    @Override
    public Object getValue() {
        Collection valueList = getValueList();
        Collection result = new ArrayList();
        for (Object o : valueList) {
            result.add(fieldValueFormatter.formatFieldValue(o));
        }
        return result;
    }

    public Collection getValueList() {
        return getDataSource().getItemIds();
    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {
        super.setPropertyDataSource(newDataSource);
        setDataSource((Collection) newDataSource.getValue());
    }

    public void setDataSource(Collection values) {
        getDataSource().addAll(values);
        table.refreshRowCache();
        //table.requestRepaint();
        for (Object o : values) {
            Object item = fieldValueFormatterReverse.formatFieldValue(o);
            table.addItem(item);
        }
        table.requestRepaint();
        this.targetCollection = values;
    }

    @Override
    public void commit() throws SourceException, Validator.InvalidValueException {
        //validate();
        targetCollection.clear();
        targetCollection.addAll((Collection) getValue());
        log.debug("commit MultipleSelect, targetList: " + targetCollection);
    }

    private BeanItemContainer getDataSource() {
        return ((BeanItemContainer)table.getContainerDataSource());
    }

    @Override
    public void setDebugId(String id) {
        super.setDebugId(id);
        field.setDebugId(id + "_combo");
        addButton.setDebugId(id + "_add");
        table.setDebugId(id + "_table");
    }

    /*
    @Override
    public void addValidator(Validator validator) {
        log.debug(" ========== MultipleSelect.addValidator: " + validator);
        this.beanValidator = validator;
    }

    @Override
    public void validate() throws Validator.InvalidValueException {
        log.debug(" ========== MultipleSelect.validate, value: " + getValue());

        // temp poc validointi koska vaatimen validointi listoille on rikki

        try {
            javax.validation.Validator validator = (javax.validation.Validator) getField("validator").get(beanValidator);
            Class beanClass = (Class) getField("beanClass").get(beanValidator);
            String propertyName = (String) getField("propertyName").get(beanValidator);
            Object value = getValue();
            Set<ConstraintViolation> violations = validator.validateValue(beanClass, propertyName, value, new Class[0]);
            log.debug("    VALIDATOR: " + validator + ", BEANCLASS: " + beanClass + ", PROPERTY: " + propertyName + ", VALUE: " + value);
            log.debug("    VIOLATIONS: " + violations);
            if (!violations.isEmpty()) {
                throw new Validator.InvalidValueException(violations.iterator().next().getMessage());
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            if (e1 instanceof Validator.InvalidValueException) {
                throw (Validator.InvalidValueException) e1;
            }
        }
    }
    */

    public FIELDCLASS getField() {
        return field;
    }

    public void setField(FIELDCLASS field) {
        this.field = field;
        searchAndAdd.addComponent(field, 0);
    }

    public Table getTable() {
        return table;
    }

    public Button getAddButton() {
        return addButton;
    }

    public void setFieldValueFormatter(FieldValueFormatter fieldValueFormatter) {
        this.fieldValueFormatter = fieldValueFormatter;
    }

    public void setFieldValueFormatterReverse(FieldValueFormatter fieldValueFormatterReverse) {
        this.fieldValueFormatterReverse = fieldValueFormatterReverse;
    }
}
