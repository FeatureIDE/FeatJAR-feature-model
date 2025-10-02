package de.featjar.feature.model.analysis;
import static org.junit.jupiter.api.Assertions.*;

import de.featjar.Common;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.FeatureTree;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureTree;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class SimpleTreePropertiesTest extends Common {
    SimpleTreeProperties simpleTreeProperties = new SimpleTreeProperties();
    IFeatureTree smallTree = generateSmallTree();

    /**
     * Creates a tree with a root node that has 1 child, and this child has 2 more children
     * @return a small feature tree for testing purposes
     */
    private IFeatureTree generateSmallTree() {
        FeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
        IFeatureTree rootTree =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
        rootTree.mutate().toAlternativeGroup();

        IFeature childFeature1 = featureModel.mutate().addFeature("Test1");
        IFeatureTree childTree1 = rootTree.mutate().addFeatureBelow(childFeature1);
        IFeature childFeature2 = featureModel.mutate().addFeature("Test2");
        childTree1.mutate().addFeatureBelow(childFeature2);
        IFeature childFeature3 = featureModel.mutate().addFeature("Test3");
        childTree1.mutate().addFeatureBelow(childFeature3);

        return rootTree;
    }

    private IFeatureTree generateMediumTree() {
        // why does every regular feature without a .mutate().toXGroup() call become an AndGroup of presumably one?
        FeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
        IFeatureTree treeRoot =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("ConfigDB"));

        IFeature featureAPI = featureModel.mutate().addFeature("API");
        IFeatureTree treeAPI = treeRoot.mutate().addFeatureBelow(featureAPI);
        treeAPI.isMandatory();
        // treeRoot.addChild(treeAPI);

        IFeature featureGet = featureModel.mutate().addFeature("Get");
        IFeatureTree treeGet = treeAPI.mutate().addFeatureBelow(featureGet);
        //treeAPI.addChild(treeGet);

        IFeature featurePut = featureModel.mutate().addFeature("Put");
        IFeatureTree treePut = treeAPI.mutate().addFeatureBelow(featurePut);
        //treeAPI.addChild(treePut);

        IFeature featureDelete = featureModel.mutate().addFeature("Delete");
        IFeatureTree treeDelete = treeAPI.mutate().addFeatureBelow(featureDelete);
        //treeAPI.addChild(treeDelete);
        treeAPI.mutate().toOrGroup();

        IFeature featureTransactions = featureModel.mutate().addFeature("Transactions");
        IFeatureTree treeTransactions = treeRoot.mutate().addFeatureBelow(featureTransactions);
        treeTransactions.isOptional();

        IFeature featureOS = featureModel.mutate().addFeature("OS");
        IFeatureTree treeOS = treeRoot.mutate().addFeatureBelow(featureOS);
        treeOS.isMandatory();
        //treeRoot.addChild(treeOS);

        IFeature featureWindows = featureModel.mutate().addFeature("Windows");
        IFeatureTree treeWindows = treeOS.mutate().addFeatureBelow(featureWindows);
        //treeOS.addChild(treeWindows);

        IFeature featureLinux = featureModel.mutate().addFeature("Linux");
        IFeatureTree treeLinux = treeOS.mutate().addFeatureBelow(featureLinux);
        treeOS.mutate().toAlternativeGroup();
        //treeOS.addChild(treeLinux);

        return treeRoot;

    }

    @Test
    void testTopFeatures() {
        int rootChildren = simpleTreeProperties.topFeatures(smallTree).get();
        assertEquals(1, rootChildren);
    }

    @Test
    void testLeafFeaturesCounter() {
        int leaves = simpleTreeProperties.leafFeaturesCounter(smallTree).get();
        assertEquals(2, leaves);
    }

    @Test
    void testTreeDepth() {
        int depth = simpleTreeProperties.treeDepth(smallTree).get();
        assertEquals(3, depth);
    }

    @Test
    void testAvgNumberOfChildren() {
        float average = simpleTreeProperties.avgNumberOfChildren(smallTree).get();
        assertEquals(0.75, average);
    }

    @Test
    void testGroupDistribution() {
        HashMap<String, Integer> groupCounts = simpleTreeProperties.groupDistribution(smallTree).get();
        assertEquals(1, groupCounts.get("AlternativeGroup"));
        assertEquals(3, groupCounts.get("AndGroup"));
        assertEquals(0, groupCounts.get("OrGroup"));
    }

    @Test
    // to be deleted. This is to find out why everything in our tests has an and group
    void smallTest() {
        FeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
        IFeatureTree rootTree =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));

        HashMap<String, Integer> groupCounts = simpleTreeProperties.groupDistribution(rootTree).get();
        System.out.println(groupCounts);

        for (FeatureTree.Group group : rootTree.getChildrenGroups()) {
            System.out.println(group.isAnd());
        }
        System.out.println();

    }

    @Test
    void mediumTest() {
        IFeatureTree tree = generateMediumTree();
        HashMap<String, Integer> groupCounts = simpleTreeProperties.groupDistribution(tree).get();
        System.out.println(groupCounts);
    }

}
