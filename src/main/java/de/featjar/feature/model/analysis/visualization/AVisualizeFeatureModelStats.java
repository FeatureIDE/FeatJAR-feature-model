package de.featjar.feature.model.analysis.visualization;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.tree.Trees;
import de.featjar.feature.model.analysis.AnalysisTree;
import de.featjar.feature.model.analysis.visitor.AnalysisTreeVisitor;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.internal.chartpart.Chart;

import java.util.*;

/**
 * A class that builds a visualization using the XChart library.
 * Data is read as an {@link AnalysisTree} and the chart is built by the buildChart() methods of each child class.
 *
 * @author Benjamin von Holt
 * @author Valentin Laubsch
 */
public abstract class AVisualizeFeatureModelStats {

    final protected AnalysisTree<?> analysisTree;
    protected HashMap<String, Object> analysisTreeData = null; // todo maybe make this a linked hash map if we sort trees alphabetically and want to remember the order
    private Chart<?, ?> chart;

    private String chartTitle = "Chart";
    private Integer width;
    private Integer height;


    public AVisualizeFeatureModelStats(AnalysisTree<?> analysisTree) {
        this.analysisTree = analysisTree;
        try {
            this.extractAnalysisTree();
        } catch (Exception e) {
            System.out.println(e);
        }
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
     * {@return String key used to fetch data from the Analysis Tree later.}
     */
    protected abstract String getAnalysisTreeDataName();

    /**
     * uses internal analysisTree and getAnalysisTreeDataName() to fetch the needed piece of data.
     */
    public void extractAnalysisTree() throws RuntimeException {
        // traverse tree to extract the general HashMap that more specific information is stored in.
        AnalysisTreeVisitor visitor = new AnalysisTreeVisitor();
        Result<HashMap<String, Object>> result = Trees.traverse(analysisTree, visitor);
        HashMap<String, Object> receivedResult = result.get();
        assert receivedResult != null: "Analysis Tree Visitor failed to produce a result.";
        @SuppressWarnings("unchecked")
        HashMap<String, Object> analysisMap = (HashMap<String, Object>) receivedResult.get("Analysis");
        assert analysisMap != null: "Received no \"Analysis\" HashMap from AnalysisTree";

        // build keys for each tree
        String statName = this.getAnalysisTreeDataName();
        Set<String> analysisMapKeys = analysisMap.keySet();
        ArrayList<String> relevantKeys = new ArrayList<>();
        for (String key : analysisMapKeys) {
            if (key.contains(statName)) {
                relevantKeys.add(key);
            }
        }

        // for each tree
        HashMap<String, Object>  analysisTreeData = new HashMap<>();
        for (String key : relevantKeys) {
            Object attributeResult = analysisMap.get(key);
            assert attributeResult != null : "Could not retrieve data called " + key + " from AnalysisTree.";

            if (attributeResult instanceof Map) {
                // todo instead of transferring the whole map, only transfer its get(2) value
                @SuppressWarnings("unchecked")
                // attributeResult.get() oder nur attributeResult?
                HashMap<String, Object> nestedMap = (HashMap<String, Object>) attributeResult.get();
                Set<String> groupKeys = nestedMap.keySet();
                // TODO sort keys for trees and in alphabetical order
                //nicht getestet //Set<String> sortedGroupKeys = FeatJAR.sortSetAlphabetically(groupKeys);
                for (String groupKey : groupKeys) {
                    ArrayList<?> groupResult = (ArrayList<?>) nestedMap.get(groupKey);
                    String resultKey = key + " " + groupKey;
                    // TODO check if index 2 is always correct, or if there is a better way to access the value
                    analysisTreeData.put(resultKey, groupResult.get(2));

                }
            } else if (attributeResult instanceof ArrayList) {
                analysisTreeData.put(key, ((ArrayList<?>) attributeResult).get(2));
            } else {
                throw new RuntimeException("Analysis Tree contained unknown data type in key " + key);
            }
        }

        this.analysisTreeData = analysisTreeData;
    }

    /**
     * You can use the analysisTreeData array list to access the analysisTree data relevant for building your chart.
     * @return the chart that will be used by the other class methods
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
