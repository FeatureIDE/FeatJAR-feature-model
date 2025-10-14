package de.featjar.feature.model.analysis.visualization;

import de.featjar.feature.model.analysis.AnalysisTree;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.internal.chartpart.Chart;

/**
 * A class that builds a visualization using the XChart library.
 * Data is read as an {@link AnalysisTree} and the chart is built by the buildChart() methods of each child class.
 *
 * @author Benjamin von Holt
 * @author Valentin Laubsch
 */
public abstract class AVisualizeFeatureModelStats {

    final private AnalysisTree<?> analysisTree;
    private Chart<?, ?> chart;

    private String chartTitle = "Chart";
    private Integer width;
    private Integer height;


    public AVisualizeFeatureModelStats(AnalysisTree<?> analysisTree) {
        this.analysisTree = analysisTree;
        this.chart = buildChart();
    }

    public AVisualizeFeatureModelStats(AnalysisTree<?> analysisTree, String chartTitle) {
        this(analysisTree);
        this.chartTitle = chartTitle;
    }

    public Chart<?, ?> getChart() {
        return this.chart;
    }

    public Integer getWidth() {
        return this.width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return this.height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    /**
     *
     * {@return the chart that will be used by the other class methods}
     */
    abstract Chart<?, ?> buildChart();


    /**
     * Creates a live preview pop-up window of the internally generated chart.
     */
    public void displayChart() {
        new SwingWrapper<>(this.chart).displayChart();
    }

    // TODO pdf export hinzufügen

}
