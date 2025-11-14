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

/**
 * Utility for SeriesStats to integrate arrays in {@link AnalysisTree}
 *
 *
 * @author Valentin Laubsch
 */
public final class SeriesStats {
    private SeriesStats() {}

    public static double avg(double[] a) {
        if (a.length == 0) return 0.0;
        double s = 0.0;
        for (double v : a) s += v;
        return s / a.length;
    }

    public static double median(double[] a) {
        if (a.length == 0) return 0.0;
        double[] c = a.clone();
        Arrays.sort(c);
        int n = c.length;
        return (n % 2 == 1) ? c[n / 2] : (c[n / 2 - 1] + c[n / 2]) / 2.0;
    }

    public static double min(double[] a) {
        if (a.length == 0) return 0.0;
        double m = a[0];
        for (double v : a) if (v < m) m = v;
        return m;
    }

    public static double max(double[] a) {
        if (a.length == 0) return 0.0;
        double m = a[0];
        for (double v : a) if (v > m) m = v;
        return m;
    }
}
