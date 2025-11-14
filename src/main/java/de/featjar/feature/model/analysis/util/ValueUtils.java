package de.featjar.feature.model.analysis.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;

public final class ValueUtils {
    private ValueUtils() {}

    public static boolean equalsValue(Object a, Object b) {
        if (a == b) return true;
        if (a == null || b == null) return false;

        if (a.getClass().isArray() && b.getClass().isArray()) {
            int lenA = Array.getLength(a);
            int lenB = Array.getLength(b);
            if (lenA != lenB) return false;
            for (int i = 0; i < lenA; i++) {
                Object elemA = Array.get(a, i);
                Object elemB = Array.get(b, i);
                if (!Objects.equals(elemA, elemB)) return false;
            }
            return true;
        }
        return Objects.equals(a, b);
    }

    public static int hashValue(Object v) {
        if (v == null) return 0;
        if (v.getClass().isArray()) {
            int len = Array.getLength(v);
            int hash = 1;
            for (int i = 0; i < len; i++) {
                Object elem = Array.get(v, i);
                hash = 31 * hash + Objects.hashCode(elem);
            }
            return hash;
        }
        return v.hashCode();
    }

    public static String toStringValue(Object v) {
        if (v == null) return "null";
        if (v.getClass().isArray()) {
            int len = Array.getLength(v);
            Object[] boxed = new Object[len];
            for (int i = 0; i < len; i++) boxed[i] = Array.get(v, i);
            return Arrays.toString(boxed);
        }
        return v.toString();
    }
}
