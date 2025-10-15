package de.featjar.feature.model.analysis.visualization;

import de.featjar.feature.model.analysis.AnalysisTree;
import org.knowm.xchart.internal.chartpart.Chart;

import java.util.ArrayList;

public class VisualizeAverageNumberOfChildren extends AVisualizeFeatureModelStats {
    public VisualizeAverageNumberOfChildren(AnalysisTree<?> analysisTree) { super(analysisTree); }

    /**
     * {@return String key used to fetch data from the Analysis Tree.}
     */
    @Override
    protected String getAnalysisTreeDataName() { return "Average Number of Children"; }

    /**
     * Use analysisTreeData to access the data relevant for building your chart.
     * There are also premade builders that you may adopt.
     *
     * @return list containing one chart per tree in the feature model
     */
    @Override
    ArrayList<Chart<?, ?>> buildCharts() { return buildBoxCharts(); }
}
