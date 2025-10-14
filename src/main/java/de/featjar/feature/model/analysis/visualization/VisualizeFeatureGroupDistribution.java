package de.featjar.feature.model.analysis.visualization;

import de.featjar.feature.model.analysis.AnalysisTree;
import org.knowm.xchart.internal.chartpart.Chart;

public class VisualizeFeatureGroupDistribution extends AVisualizeFeatureModelStats{
    public VisualizeFeatureGroupDistribution(AnalysisTree<?> analysisTree) {
        super(analysisTree);
    }

    public VisualizeFeatureGroupDistribution(AnalysisTree<?> analysisTree, String chartTitle) {
        super(analysisTree, chartTitle);
    }

    /**
     *
     * {@return the chart that will be used by the other class methods}
     */
    @Override
    Chart<?, ?> buildChart() {
        return null;
    }
}
