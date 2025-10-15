package de.featjar.feature.model.analysis.visualization;

import de.featjar.feature.model.analysis.AnalysisTree;
import org.knowm.xchart.internal.chartpart.Chart;

import java.util.ArrayList;

public class VisualizeFeatureGroupDistribution extends AVisualizeFeatureModelStats{
    public VisualizeFeatureGroupDistribution(AnalysisTree<?> analysisTree) {
        super(analysisTree);
    }

    /**
     *
     * {@return String key used to fetch data from the Analysis Tree.}
     */
    @Override
    protected String getAnalysisTreeDataName() {
        return "Number of Top Features";
        //return "Group Distribution";
    }

    /**
     * You can use the analysisTreeData array list to access the analysisTree data relevant for building your chart.
     * @return the chart that will be used by the other class methods
     */
    @Override
    ArrayList<Chart<?, ?>> buildCharts() {
        return buildPieCharts();
    }
}
