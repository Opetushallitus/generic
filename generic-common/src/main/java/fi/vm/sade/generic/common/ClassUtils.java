package fi.vm.sade.generic.common;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ClassUtils {

    private ClassUtils() {
    }

    public static String toString(Object o) {
        return ToStringBuilder.reflectionToString(o);
        /*
        ArrayList list = new ArrayList();
        ClassUtils.toString(o, o.getClass(), list);
        return o.getClass().getName().concat(list.toString());
        */
    }

    /*
    private static void toString(Object o, Class clazz, List list) {
        List<Field> fields = getDeclaredFields(clazz);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                list.add(field.getName() + "=" + field.get(o));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (clazz.getSuperclass().getSuperclass() != null) {
            toString(o, clazz.getSuperclass(), list);
        }
    }
    */

    public static List<Field> getDeclaredFields(Class clazz) {
        List<Field> fields = new ArrayList<Field>();
        Class tempClass = clazz;
        while (true) {
            fields.addAll(Arrays.asList(tempClass.getDeclaredFields()));
            if (tempClass == Object.class) {
                break;
            } else {
                tempClass = tempClass.getSuperclass();
            }
        }
        return fields;
    }

    public static Field getDeclaredField(Class clazz, String fieldName) {
        Field field;
        Class tempClass = clazz;
        while (true) {
            try {
                field = tempClass.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException e) {
                if (tempClass == Object.class) {
                    throw new RuntimeException("field not found from class hierarchy, field: " + fieldName + ", class: " + clazz);
                } else {
                    tempClass = tempClass.getSuperclass();
                }
            }
        }
        return field;
    }

}