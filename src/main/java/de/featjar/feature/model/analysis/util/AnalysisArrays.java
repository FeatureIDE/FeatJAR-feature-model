/*
 * Copyright (C) 2025 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-feature-model.
 *
 * feature-model is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * feature-model is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with feature-model. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-feature-model> for further information.
 */
package de.featjar.feature.model.analysis.util;

import de.featjar.feature.model.analysis.AnalysisTree;

import java.util.Arrays;
import java.util.List;

/**
 * Utility for Series to integrate arrays in {@link AnalysisTree}
 *
 *
 * @author Valentin Laubsch
 */
public final class AnalysisArrays {
    private AnalysisArrays() {}

    /** Returns true if v is a numeric series: primitive number array or List<Number> (empty list counts as series). */
    public static boolean isSeries(Object v) {
        if (v == null) return false;
        if (v instanceof double[] || v instanceof int[] || v instanceof long[] || v instanceof float[]) {
            return true;
        }
        if (v instanceof List<?>) {
            List<?> l = (List<?>) v;
            if (l.isEmpty()) return true; // treat empty as a valid (empty) series
            for (Object e : l) {
                if (!(e instanceof Number)) return false;
            }
            return true;
        }
        return false;
    }

    /** Converts any supported series (double[]/int[]/long[]/float[]/List<Number>) to a double[]. Unknown → empty array. */
    public static double[] toDoubleArray(Object v) {
        if (v instanceof double[]) return (double[]) v; // no copy on purpose
        if (v instanceof int[]) {
            int[] a = (int[]) v;
            double[] d = new double[a.length];
            for (int i = 0; i < a.length; i++) d[i] = a[i];
            return d;
        }
        if (v instanceof long[]) {
            long[] a = (long[]) v;
            double[] d = new double[a.length];
            for (int i = 0; i < a.length; i++) d[i] = a[i];
            return d;
        }
        if (v instanceof float[]) {
            float[] a = (float[]) v;
            double[] d = new double[a.length];
            for (int i = 0; i < a.length; i++) d[i] = a[i];
            return d;
        }
        if (v instanceof List<?>) {
            List<?> l = (List<?>) v;
            double[] d = new double[l.size()];
            for (int i = 0; i < l.size(); i++) {
                Object e = l.get(i);
                d[i] = (e instanceof Number) ? ((Number) e).doubleValue() : Double.NaN;
            }
            return d;
        }
        return new double[0];
    }

    public static String toReadableString(Object v) {
        if (v instanceof int[])    return Arrays.toString((int[]) v);
        if (v instanceof double[]) return Arrays.toString((double[]) v);
        if (v instanceof long[])   return Arrays.toString((long[]) v);
        if (v instanceof float[])  return Arrays.toString((float[]) v);
        if (v instanceof List<?>)  return toReadableString(toDoubleArray(v));
        return String.valueOf(v);
    }
}
