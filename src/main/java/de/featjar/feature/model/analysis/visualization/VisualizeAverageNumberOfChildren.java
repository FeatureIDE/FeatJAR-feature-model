package de.featjar.feature.model.analysis.visualization;

import de.featjar.feature.model.analysis.AnalysisTree;
import org.knowm.xchart.internal.chartpart.Chart;

import java.util.ArrayList;

/**
 * Visualizes and exports the feature model statistic "Average Number of Children".
 * Data is read as an {@link AnalysisTree}. Each child specifies the information to be read from the tree via
 * {@link #getAnalysisTreeDataName()}, as well as how to build a chart from it via the {@link #buildCharts()} method.
 *
 * @author Benjamin von Holt
 * @author Valentin Laubsch
 */
public class VisualizeAverageNumberOfChildren extends AVisualizeFeatureModelStats {
    /**
     * Visualizes and exports the feature model statistic "Average Number of Children".
     *
     * @param analysisTree {@link AnalysisTree} over the entire feature model.
     */
    public VisualizeAverageNumberOfChildren(AnalysisTree<?> analysisTree) { super(analysisTree); }

    @Override
    protected String getAnalysisTreeDataName() {return "Average Number of Children";}

    @Override
    protected ArrayList<Chart<?, ?>> buildCharts() {return buildBoxCharts();}
}
