package de.featjar.feature.model.visualization;
import de.featjar.base.data.Result;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.base.io.IO;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.analysis.AnalysisTree;
import de.featjar.feature.model.analysis.visualization.VisualizeGroupDistribution;
import de.featjar.feature.model.cli.PrintStatistics;
import de.featjar.feature.model.io.transformer.AnalysisTreeTransformer;
import de.featjar.feature.model.io.xml.XMLFeatureModelFormat;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class VisualizeFeatureModelStatsTest {
    AnalysisTree<?> bigTree = getBigAnalysisTree();
    AnalysisTree<?> mediumTree = getMediumAnalysisTree();
    String defaultExportName = "src/test/java/de/featjar/feature/model/visualization/model.xml";

    /**
     * Helper function.
     * Yields feature model with a single tree. This feature tree has three nodes under the root:
     * API is mandatory and below it is an or-group with the features Get, Put, Delete.
     * OS is also mandatory and below it is an alternative group with the features Windows, Linux.
     * Transactions is an optional feature below the root.
     * @return a medium-sized feature model for testing purposes.
     */
    public FeatureModel buildMediumFeatureModel() {
        FeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
        IFeatureTree treeRoot =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("ConfigDB"));

        IFeature featureAPI = featureModel.mutate().addFeature("API");
        IFeatureTree treeAPI = treeRoot.mutate().addFeatureBelow(featureAPI);
        treeAPI.isMandatory();
        IFeature featureGet = featureModel.mutate().addFeature("Get");
        treeAPI.mutate().addFeatureBelow(featureGet);
        IFeature featurePut = featureModel.mutate().addFeature("Put");
        treeAPI.mutate().addFeatureBelow(featurePut);
        IFeature featureDelete = featureModel.mutate().addFeature("Delete");
        treeAPI.mutate().addFeatureBelow(featureDelete);
        treeAPI.mutate().toOrGroup();

        IFeature featureOS = featureModel.mutate().addFeature("OS");
        IFeatureTree treeOS = treeRoot.mutate().addFeatureBelow(featureOS);
        treeOS.isMandatory();
        IFeature featureWindows = featureModel.mutate().addFeature("Windows");
        treeOS.mutate().addFeatureBelow(featureWindows);
        IFeature featureLinux = featureModel.mutate().addFeature("Linux");
        treeOS.mutate().addFeatureBelow(featureLinux);
        treeOS.mutate().toAlternativeGroup();

        IFeature featureTransactions = featureModel.mutate().addFeature("Transactions");
        IFeatureTree treeTransactions = treeRoot.mutate().addFeatureBelow(featureTransactions);
        treeTransactions.isOptional();

        return featureModel;
    }

    /**
     * Helper function. Converts a feature model into an {@link AnalysisTree}
     */
    public AnalysisTree<?> analysisTreeFromFeatureModel(FeatureModel featureModel) {
        PrintStatistics printStatistics = new PrintStatistics();
        LinkedHashMap<String, Object> map = printStatistics.collectStats(
                featureModel,
                PrintStatistics.AnalysesScope.ALL
        );
        return AnalysisTreeTransformer.hashMapToTree(map, "Analysis").get();
    }

    public AnalysisTree<?> analysisTreeFromXML (Path path) {
        Result<IFeatureModel> load = IO.load(path, new XMLFeatureModelFormat());
        FeatureModel model = (FeatureModel) load.orElseThrow();

        PrintStatistics printStatistics = new PrintStatistics();
        LinkedHashMap<String, Object> map = printStatistics.collectStats(
                model,
                PrintStatistics.AnalysesScope.ALL
        );
        return AnalysisTreeTransformer.hashMapToTree(map, "Analysis").get();
    }

    public AnalysisTree<?> getBigAnalysisTree () {
        return analysisTreeFromXML(Paths.get("src/test/java/de/featjar/feature/model/visualization/model.xml"));
    }

    public AnalysisTree<?> getMediumAnalysisTree() {
        return analysisTreeFromFeatureModel(buildMediumFeatureModel());
    }

    // todo auch Tests für die andere stats art mit constraints bla

    @Test
    void regularLivePreview() {
        VisualizeGroupDistribution viz;

        viz = new VisualizeGroupDistribution(mediumTree);
        viz.displayChart();

        viz = new VisualizeGroupDistribution(bigTree);
        viz.displayChart();

        // todo have to think about how to test this
        assertTrue(true);
    }

    @Test
    void pdfValidIndex() {
        VisualizeGroupDistribution viz;

        viz = new VisualizeGroupDistribution(mediumTree);
        assertEquals(0, viz.exportChartToPDF(0, defaultExportName));

        viz = new VisualizeGroupDistribution(bigTree);
        assertEquals(0, viz.exportChartToPDF(0, defaultExportName));
    }

    @Test
    void pdfInvalidIndex() {
        VisualizeGroupDistribution viz;

        viz = new VisualizeGroupDistribution(mediumTree);
        assertEquals(1, viz.exportChartToPDF(99, defaultExportName));

        viz = new VisualizeGroupDistribution(bigTree);
        assertEquals(1, viz.exportChartToPDF(99, defaultExportName));
    }
}
