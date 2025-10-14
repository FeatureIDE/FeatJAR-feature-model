package de.featjar.feature.model.analysis.visualization;

import de.featjar.feature.model.analysis.AnalysisTree;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.internal.chartpart.Chart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class VisualizeFeatureGroupDistribution extends AVisualizeFeatureModelStats{
    public VisualizeFeatureGroupDistribution(AnalysisTree<?> analysisTree) {
        super(analysisTree, "Group Distribution");
    }

    public VisualizeFeatureGroupDistribution(AnalysisTree<?> analysisTree, String chartTitle) {
        super(analysisTree, chartTitle);
    }

    /**
     *
     * {@return String key used to fetch data from the Analysis Tree later.}
     */
    @Override
    protected String getAnalysisTreeDataName() {
        return "Group Distribution";
    }

    /**
     * You can use the analysisTreeData array list to access the analysisTree data relevant for building your chart.
     * @return the chart that will be used by the other class methods
     */
    @Override
    Chart<?, ?> buildChart() {

        PieChart chart1 = new PieChartBuilder().build();

        for (String key : this.analysisTreeData.keySet()) { // für jede "gruppe pro Baum"
            chart1.addSeries(key, (Integer) this.analysisTreeData.get(key));
        }
        
        
        
        /*
        for (String key : this.analysisTreeData.keySet()) { // für jeden Baum
            @SuppressWarnings("unchecked")
            HashMap<String, Object> nestedMap = (HashMap<String, Object>) this.analysisTreeData.get(key);
            Set<String> groupDistributionKeys = nestedMap.keySet();

            for (String groupKey : groupDistributionKeys) {
                ArrayList<?> groupResult = (ArrayList<?>) nestedMap.get(groupKey);
                chart1.addSeries(groupKey, (Integer) groupResult.get(2));
            }
        }*/

        // placeholder
        /*
        PieChart chart1 = new PieChartBuilder().build();
        chart1.addSeries("Banananana", 40);
        chart1.addSeries("Apfel", 25);
        chart1.addSeries("Gurke", 50);

         */

        // data that we actually want to use
        // System.out.println(this.analysisTreeData);

        return chart1;
    }
}
