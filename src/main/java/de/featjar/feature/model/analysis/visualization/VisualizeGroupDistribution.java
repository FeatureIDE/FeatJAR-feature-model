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
package de.featjar.feature.model.analysis.visualization;

import de.featjar.feature.model.analysis.AnalysisTree;
import java.util.ArrayList;
import org.knowm.xchart.internal.chartpart.Chart;

/**
 * Visualizes and exports the feature model statistic "Group Distribution".
 * Data is read as an {@link AnalysisTree}. Each child specifies the information to be read from the tree via
 * {@link #getAnalysisTreeDataName()}, as well as how to build a chart from it via the {@link #buildCharts()} method.
 *
 * @author Benjamin von Holt
 * @author Valentin Laubsch
 */
public class VisualizeGroupDistribution extends AVisualizeFeatureModelStats {

    /**
     * Visualizes and exports the feature model statistic "Operator Distribution".
     *
     * @param analysisTree {@link AnalysisTree} over the entire feature model.
     */
    public VisualizeGroupDistribution(AnalysisTree<?> analysisTree) {
        super(analysisTree);
    }

    @Override
    protected String getAnalysisTreeDataName() {
        return "Group Distribution";
    }

    @Override
    protected ArrayList<Chart<?, ?>> buildCharts() {
        return buildPieCharts();
    }
}
