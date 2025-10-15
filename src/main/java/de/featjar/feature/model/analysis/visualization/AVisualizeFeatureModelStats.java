package de.featjar.feature.model.analysis.visualization;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.tree.Trees;
import de.featjar.feature.model.analysis.AnalysisTree;
import de.featjar.feature.model.analysis.visitor.AnalysisTreeVisitor;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.internal.chartpart.Chart;

import java.util.*;
import java.util.stream.Collectors;

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
    final protected ArrayList<Chart<?, ?>> charts;

    private Integer width = 800;
    private Integer height = 600;

    public AVisualizeFeatureModelStats(AnalysisTree<?> analysisTree) {
        this.analysisTree = analysisTree;
        this.analysisTreeData = extractAnalysisTree();
        this.charts = buildCharts();
        chartsAreEmpty();
    }

    public ArrayList<Chart<?, ?>> getCharts() {
        return this.charts;
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
     * Checks if any charts are available and if not reminds the user of this fact with a warning message.
     * @return True if charts exist, else false.
     */
    protected boolean chartsAreEmpty() {
        if (this.charts.isEmpty()) {
            FeatJAR.log().warning(this.getClass().getName() + " did not build any charts!");
            return true;
        }
        return false;
    }

    /**
     * {@return String key used to fetch data from the Analysis Tree.}
     */
    protected abstract String getAnalysisTreeDataName();

    /**
     * Uses internal analysisTree and getAnalysisTreeDataName() to fetch the needed piece of data from the analysis tree.
     * <p>
     * {@return alphabetically sorted map with one key per tree.
     * Each value is another HashMap with one entry per piece of data extracted from the Analysis Tree. These pieces of data are
     * also alphabetically sorted.}
     */
    public LinkedHashMap<String, LinkedHashMap<String, Object>> extractAnalysisTree() throws RuntimeException {
        HashMap<String, Object> analysisMap = extractAnalysisMap();

        // fetches keys for all trees for the data we want
        // example: [Tree 1] Group Distribution, [Tree 2] Group Distribution, ...
        List<String> featureTreeDataKeys = analysisMap.keySet().stream()
                .filter(key -> key.contains(this.getAnalysisTreeDataName()))
                .sorted()
                .collect(Collectors.toList());

        // preparing return value
        LinkedHashMap<String, LinkedHashMap<String, Object>> analysisTreeData = new LinkedHashMap<>();

        // for each tree: add a HashMap containing values per piece of information we need to extract
        for (String key : featureTreeDataKeys) {
            LinkedHashMap<String, Object> featureTreeData = new LinkedHashMap<>();
            Object pieceOfInformation = analysisMap.get(key);
            assert pieceOfInformation != null : "Could not retrieve data called \"" + key + "\" from AnalysisTree.";

            if (pieceOfInformation instanceof Map) {
                @SuppressWarnings("unchecked")
                HashMap<String, Object> nestedMap = (HashMap<String, Object>) pieceOfInformation;

                nestedMap.keySet().stream()
                        .sorted()
                        .forEach(nestedMapKey -> {
                            // the value relevant for us needs to be unpacked first
                            Object rawValue = nestedMap.get(nestedMapKey);
                            ArrayList<?> castedValue = (ArrayList<?>) rawValue;
                            Object value = castedValue.get(2);
                            featureTreeData.put(nestedMapKey, value);
                        });

            } else if (pieceOfInformation instanceof ArrayList) {
                ArrayList<?> castedValue = (ArrayList<?>) pieceOfInformation;
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
     * {@return the {@link AnalysisTree}'s general HashMap that more specific information is stored in.}
     */
    private HashMap<String, Object> extractAnalysisMap() {
        Result<HashMap<String, Object>> result = Trees.traverse(analysisTree, new AnalysisTreeVisitor());
        HashMap<String, Object> receivedResult = result.get();
        assert receivedResult != null: "Analysis Tree Visitor failed to produce a result.";

        // we currently trust that this is always "Analysis"
        @SuppressWarnings("unchecked")
        HashMap<String, Object> analysisMap = (HashMap<String, Object>) receivedResult.get("Analysis");
        assert analysisMap != null: "Received no \"Analysis\" HashMap from AnalysisTree";

        return analysisMap;
    }

    /**
     * Use analysisTreeData to access the data relevant for building your chart.
     * There are also premade builders that you may adopt.
     * @return list containing one chart per tree in the feature model
     */
    abstract ArrayList<Chart<?, ?>> buildCharts();

    /**
     * Premade builder for pie charts that you can use when implementing buildCharts().
     * @return list containing one chart per tree in the feature model
     */
    protected ArrayList<Chart<?, ?>> buildPieCharts() {
        ArrayList<Chart<?, ?>> charts = new ArrayList<>();
        for (String treeKey : this.analysisTreeData.keySet()) {
            PieChart chart = new PieChartBuilder()
                    .width(getWidth())
                    .height(getHeight())
                    .build();
            HashMap<String, Object> treeData = analysisTreeData.get(treeKey);
            for (String key: treeData.keySet()) {
                chart.addSeries(key, (Integer) treeData.get(key));
            }
            chart.setTitle(treeKey);

            charts.add(chart);
        }
        return charts;
    }

    /**
     * Creates a live preview pop-up window of a chart.
     */
    public void displayChart (Chart<?, ?> chart) {
        if (chartsAreEmpty()) {return;}
        new SwingWrapper<>(chart).displayChart();
    }

    /**
     * Creates a live preview pop-up window of the FIRST internally generated chart.
     * This chart usually corresponds to the first feature tree in the feature model.
     */
    public void displayChart() {
        if (chartsAreEmpty()) {return;}
        this.displayChart(0);
    }

    /**
     * Creates a live preview pop-up window of an internally generated chart, fetched by index.
     */
    public void displayChart (Integer index) {
        if (chartsAreEmpty()) {return;}
        this.displayChart(this.charts.get(index));
    }

    /**
     * Creates live preview pop-up windows of ALL internally generated charts.
     */
    public void displayAllCharts() {
        if (chartsAreEmpty()) {return;}

        for (Chart<?, ?> chart : this.charts) {
            this.displayChart(chart);
        }
    }

    // TODO pdf export

}
