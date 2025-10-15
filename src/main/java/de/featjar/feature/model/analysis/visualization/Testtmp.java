package de.featjar.feature.model.analysis.visualization;

import de.featjar.base.data.Result;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.base.io.IO;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.analysis.AnalysisTree;
import de.featjar.feature.model.cli.PrintStatistics;
import de.featjar.feature.model.io.transformer.AnalysisTreeTransformer;
import de.featjar.feature.model.io.xml.XMLFeatureModelFormat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

public class Testtmp {

    public static AnalysisTree<?> createDefaultTree() {
        AnalysisTree<?> innereanalysisTree = new AnalysisTree<>(
                "avgNumOfAtomsPerConstraints",
                new AnalysisTree<>("test property", 3.3),
                new AnalysisTree<>("numOfLeafFeatures", (float) 12.4));

        AnalysisTree<?> analysisTree = new AnalysisTree<>(
                "Analysis",
                new AnalysisTree<>("numOfLeafFeatures", (float) 12.4),
                new AnalysisTree<>("numOfTopFeatures", 3.3),
                new AnalysisTree<>("treeDepth", 3),
                new AnalysisTree<>("avgNumOfChildren", 3),
                new AnalysisTree<>("numInOrGroups", 7),
                new AnalysisTree<>("numInAltGroups", 5),
                new AnalysisTree<>("numOfAtoms", 8),
                new AnalysisTree<>("Group Distribution", 4),
                innereanalysisTree);
        return analysisTree;
    }

    public static AnalysisTree<?> generateEmptyTree() {
        FeatureModel emptyFeatureModel = new FeatureModel();
        emptyFeatureModel.mutate().addFeatureTreeRoot(emptyFeatureModel.mutate().addFeature("root"));

        PrintStatistics printStatistics = new PrintStatistics();
        LinkedHashMap<String, Object> map = printStatistics.collectStats(
                generateMediumTree(),
                PrintStatistics.AnalysesScope.ALL
        );

        return AnalysisTreeTransformer.hashMapToTree(map, "Analysis").get();
    }

    /**
     * Feature tree with three nodes under the root. API is mandatory and below it is an or-group with the features
     * Get, Put, Delete. OS is also mandatory and below it is an alternative group with the features Windows, Linux.
     * Transactions is an optional feature below the root.
     * @return a medium-sized feature tree for testing purposes.
     */
    public static FeatureModel generateMediumTree() {
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

    public static void main(String[] args) throws Exception {
        PrintStatistics printStatistics = new PrintStatistics();
        LinkedHashMap<String, Object> map = printStatistics.collectStats(
                generateMediumTree(),
                PrintStatistics.AnalysesScope.ALL
        );
        AnalysisTree<?> mediumAnalysisTree = AnalysisTreeTransformer.hashMapToTree(map, "Analysis").get();

        Path path = Paths.get("src/test/java/de/featjar/feature/model/visualization/model.xml");
        Result<IFeatureModel> load = IO.load(path, new XMLFeatureModelFormat());
        FeatureModel model = (FeatureModel) load.orElseThrow();
        map = printStatistics.collectStats(
                model,
                PrintStatistics.AnalysesScope.ALL
        );
        AnalysisTree<?> bigAnalysisTree = AnalysisTreeTransformer.hashMapToTree(map, "Analysis").get();


        VisualizeGroupDistribution viz = new VisualizeGroupDistribution(bigAnalysisTree);
        VisualizeConstraintOperatorDistribution viz2 = new VisualizeConstraintOperatorDistribution(bigAnalysisTree);
        //VisualizeFeatureGroupDistribution viz = new VisualizeFeatureGroupDistribution(createDefaultTree());
        //VisualizeFeatureGroupDistribution viz = new VisualizeFeatureGroupDistribution(generateEmptyTree());

        //viz.displayChart();
        viz2.displayChart();

    }
}
