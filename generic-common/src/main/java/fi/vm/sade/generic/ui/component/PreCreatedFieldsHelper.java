/* Decompiled through IntelliJad */
// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packfields(3) packimports(3) splitstr(64) radix(10) lradix(10)
// Source File Name:   PreCreatedFieldsHelper.java

package fi.vm.sade.generic.ui.component;

import com.vaadin.data.Item;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormFieldFactory;
import fi.vm.sade.generic.common.ClassUtils;
import org.vaadin.addon.formbinder.FormFieldMatch;
import org.vaadin.addon.formbinder.FormView;
import org.vaadin.addon.formbinder.PropertyId;

import java.lang.reflect.Field;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Referenced classes of package org.vaadin.addon.formbinder:
//            FormView, FormFieldMatch, PropertyId

// NOTE: note modified original Vaadin PreCreatedFieldsHelper to support superclasses for form view objects

public class PreCreatedFieldsHelper
    implements FormFieldFactory
{

    private static final Logger LOG = LoggerFactory.getLogger(PreCreatedFieldsHelper.class);

    public PreCreatedFieldsHelper(Object fieldSources[])
    {
        this.fieldSources = fieldSources;
    }

    public com.vaadin.ui.Field createField(Item item, Object propertyId, Component uiContext)
    {
//        LOG.info(" @@@@@@@@@@@@@@@ PreCreatedFieldsHelper, createField, propertyId: "+propertyId+", item: "+item);
        return findField(propertyId, fieldSources);
    }

    private com.vaadin.ui.Field findField(Object propertyId, Object fieldSources[])
    {
        Object aobj[];
        int j = (aobj = fieldSources).length;
        for(int i = 0; i < j; i++)
        {
            Object object = aobj[i];
            if(object != null)
            {
                com.vaadin.ui.Field f;
                if(object instanceof Object[])
                {
                    Object objs[] = (Object[])object;
                    f = findField(propertyId, objs);
                } else
                {
                    f = findField(object, propertyId);
                }
                if(f != null) {
//                    LOG.info(" @@@@@@@@@@@@@@@ PreCreatedFieldsHelper, propertyId: "+propertyId+", f: "+f.getClass().getSimpleName());
                    return f;
                }
            }
        }

//        LOG.info(" @@@@@@@@@@@@@@@ PreCreatedFieldsHelper, propertyId: "+propertyId+", f: NULL!!!");
        return null;
    }

    private com.vaadin.ui.Field findField(Object object, Object propertyId)
    {
        Class viewClass = object.getClass();
        com.vaadin.ui.Field f;
        if(viewClass.isAnnotationPresent(FormView.class)
                && ((FormView)viewClass.getAnnotation(FormView.class)).matchFieldsBy().equals(FormFieldMatch.ANNOTATION))
            f = getFieldFromAnnotatedView(object, propertyId);
        else
            f = getFieldFromNonAnnotatedView(object, propertyId);
        return f;
    }

    private com.vaadin.ui.Field getFieldFromAnnotatedView(Object view, Object propertyId)
    {
        List<Field> fields = ClassUtils.getDeclaredFields(view.getClass());
        for (Field reflectField : fields) {
            reflectField.setAccessible(true);
            try
            {
                Object viewField = reflectField.get(view);
                if(reflectField.isAnnotationPresent(PropertyId.class)
                        && ((PropertyId)reflectField.getAnnotation(PropertyId.class)).value().equals(String.valueOf(propertyId))
                        && (viewField instanceof com.vaadin.ui.Field))
                    return (com.vaadin.ui.Field)viewField;
            }
            catch(SecurityException e)
            {
                throw new RuntimeException("Field coudn't be accessed, check security manager settings.", e);
            }
            catch(IllegalArgumentException illegalargumentexception) { }
            catch(IllegalAccessException e)
            {
                throw new RuntimeException("Field coudn't be accessed, check security manager settings.", e);
            }
        }

        return null;
    }

    private com.vaadin.ui.Field getFieldFromNonAnnotatedView(Object view, Object propertyId)
    {
        try
        {
            Field field = ClassUtils.getDeclaredField(view.getClass(), (new StringBuilder(String.valueOf(propertyId.toString()))).append("Field").toString());
            field.setAccessible(true);
            Object f = field.get(view);
            if(f instanceof com.vaadin.ui.Field)
                return (com.vaadin.ui.Field)f;
        }
        catch(SecurityException e)
        {
            throw new RuntimeException("Field coudn't be accessed, check security manager settings.", e);
        }
        catch(Exception e) { }
        /*
        catch(NoSuchFieldException nosuchfieldexception) { }
        catch(IllegalArgumentException illegalargumentexception) { }
        catch(IllegalAccessException e)
        {
            throw new RuntimeException("Field coudn't be accessed, check security manager settings.", e);
        }
        */
        return null;
    }

    private Object fieldSources[];
}
