package fi.vm.sade.generic.common;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ClassUtils {

    private ClassUtils() {
    }

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

    public static void copyFields(Object o1, Object o2) {
        Class clazz1 = o1.getClass();
        Class clazz2 = o2.getClass();

        for (Field field1 : ClassUtils.getDeclaredFields(clazz1)) {
            try {
                Field field2 = ClassUtils.getDeclaredField(clazz2, field1.getName());
                field1.setAccessible(true);
                field2.setAccessible(true);
                Object value = field1.get(o1);
                field2.set(o2, value);
            } catch (Exception e) {
                throw new RuntimeException("failed to copy field '" + field1.getName(), e);
            }
        }
    }

}