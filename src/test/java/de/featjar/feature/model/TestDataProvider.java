package de.featjar.feature.model;

import de.featjar.base.data.identifier.Identifiers;
import de.featjar.feature.model.analysis.AnalysisTree;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.predicate.Literal;

public class TestDataProvider {
	
	/**
     * Feature tree with three nodes under the root. API is mandatory and below it is an or-group with the features
     * Get, Put, Delete. OS is also mandatory and below it is an alternative group with the features Windows, Linux.
     * Transactions is an optional feature below the root.
     * @return a medium-sized feature tree for testing purposes.
     */
	public static IFeatureTree generateMediumTree() {
        FeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
        IFeatureTree treeRoot =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("ConfigDB"));

        IFeature featureAPI = featureModel.mutate().addFeature("API");
        IFeature featureGet = featureModel.mutate().addFeature("Get");
        IFeature featurePut = featureModel.mutate().addFeature("Put");
        IFeature featureDelete = featureModel.mutate().addFeature("Delete");

        IFeature featureOS = featureModel.mutate().addFeature("OS");
        IFeature featureWindows = featureModel.mutate().addFeature("Windows");

        IFeatureTree treeAPI = treeRoot.mutate().addFeatureBelow(featureAPI);
        IFeatureTree treeOS = treeRoot.mutate().addFeatureBelow(featureOS);
        IFeature featureLinux = featureModel.mutate().addFeature("Linux");

        treeAPI.mutate().addFeatureBelow(featureGet);
        treeAPI.mutate().addFeatureBelow(featurePut);
        treeAPI.mutate().addFeatureBelow(featureDelete);
        treeOS.mutate().addFeatureBelow(featureWindows);
        treeOS.mutate().addFeatureBelow(featureLinux);

        treeAPI.mutate().toOrGroup();
        treeOS.mutate().toAlternativeGroup();

        treeRoot.mutate().makeMandatory();
        treeAPI.mutate().makeMandatory();
        treeOS.mutate().makeMandatory();

        return treeRoot;
    }
	
	public static FeatureModel createMediumFeatureModel() {
        FeatureModel fm = new FeatureModel();
        fm.addFeatureTreeRoot(generateMediumTree());
        fm.addConstraint(new Implies(new Literal("Transactions"), new Or(new Literal("Put"), new Literal("Delete"))));
        return fm;
    }
	
	public static AnalysisTree<?> createSmallAnalysisTree() {
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
                new AnalysisTree<>("avgNumOfAsss", 4),
                innereanalysisTree);
        return analysisTree;
    }
	
	public static FeatureModel generateModel() {
        FeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
        featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
        return featureModel;
    }
}
