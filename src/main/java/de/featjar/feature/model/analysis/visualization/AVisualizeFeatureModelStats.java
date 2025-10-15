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
    protected LinkedHashMap<String, LinkedHashMap<String, Object>> analysisTreeData;
    private Chart<?, ?> chart;

    private String chartTitle = "Chart";
    private Integer width;
    private Integer height;


    public AVisualizeFeatureModelStats(AnalysisTree<?> analysisTree) {
        this.analysisTree = analysisTree;
        try {
            this.analysisTreeData = extractAnalysisTree();
        } catch (Exception e) {
            FeatJAR.log().error(e);
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
    public LinkedHashMap<String, LinkedHashMap<String, Object>> extractAnalysisTree() throws RuntimeException {
        // traverse tree to extract the general HashMap that more specific information is stored in.
        // could probably be its own method
        AnalysisTreeVisitor visitor = new AnalysisTreeVisitor();
        Result<HashMap<String, Object>> result = Trees.traverse(analysisTree, visitor);
        HashMap<String, Object> receivedResult = result.get();
        assert receivedResult != null: "Analysis Tree Visitor failed to produce a result.";
        @SuppressWarnings("unchecked")
        HashMap<String, Object> analysisMap = (HashMap<String, Object>) receivedResult.get("Analysis"); // we currently trust that this is always "Analysis"
        assert analysisMap != null: "Received no \"Analysis\" HashMap from AnalysisTree";

        // build keys for each tree
        // example: [Tree 1] Group Distribution, [Tree 2] Group Distribution, ...
        // could probably be its own method
        String statName = this.getAnalysisTreeDataName();

        List<String> sortedAnalysisMapKeys = new ArrayList<>(analysisMap.keySet());
        Collections.sort(sortedAnalysisMapKeys);
        ArrayList<String> featureTreeDataKeys = new ArrayList<>();
        for (String key : sortedAnalysisMapKeys) {
            if (key.contains(statName)) {
                featureTreeDataKeys.add(key);
            }
        }

        // this works better with lists than arrays for some reason
        LinkedHashMap<String, LinkedHashMap<String, Object>> analysisTreeData = new LinkedHashMap<>();

        // for each tree
        for (String key : featureTreeDataKeys) {
            LinkedHashMap<String, Object> featureTreeData = new LinkedHashMap<>();
            Object attributeResult = analysisMap.get(key);
            assert attributeResult != null : "Could not retrieve data called \"" + key + "\" from AnalysisTree.";

            if (attributeResult instanceof Map) {
                @SuppressWarnings("unchecked")
                HashMap<String, Object> nestedMap = (HashMap<String, Object>) attributeResult;

                // sort keys alphabetically for consistency in the order they'll be displayed on the chart
                List<String> sortedNestedMapKeys = new ArrayList<>(nestedMap.keySet());
                Collections.sort(sortedNestedMapKeys);

                for (String nestedMapKey : sortedNestedMapKeys) {
                    // the value relevant for us needs to be unpacked first
                    Object rawValue = nestedMap.get(nestedMapKey);
                    ArrayList<?> castedValue = (ArrayList<?>) rawValue;
                    Object value = castedValue.get(2);

                    featureTreeData.put(nestedMapKey, value);
                }
            } else if (attributeResult instanceof ArrayList) {
                ArrayList<?> castedValue = (ArrayList<?>) attributeResult;
                Object value = castedValue.get(2);
                featureTreeData.put(getAnalysisTreeDataName(), value);
            } else {
                throw new RuntimeException("Analysis Tree contained unknown data type in key " + key);
            }
            analysisTreeData.put(key, featureTreeData);
        }
        return analysisTreeData;
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
