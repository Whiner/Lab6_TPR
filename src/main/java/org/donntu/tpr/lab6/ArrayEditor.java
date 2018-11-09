package org.donntu.tpr.lab6;

import java.util.ArrayList;
import java.util.List;

public class ArrayEditor {
    public static List<String> castToStringList(Object[] objects) {
        List<String> list = new ArrayList<String>();
        for (final Object object : objects) {
            list.add((String) object);
        }
        return list;
    }

    public static Integer[] castToInteger(Object[] objects) {
        Integer[] integers = new Integer[objects.length];
        for (int i = 0; i < objects.length; i++) {
            integers[i] = (Integer) objects[i];
        }
        return integers;
    }

}
