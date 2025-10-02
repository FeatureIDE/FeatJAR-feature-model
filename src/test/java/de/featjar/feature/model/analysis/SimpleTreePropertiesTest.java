package de.featjar.feature.model.analysis;
import static org.junit.jupiter.api.Assertions.*;

import de.featjar.Common;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.feature.model.FeatureModel;
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
        IFeatureTree childTree2 = childTree1.mutate().addFeatureBelow(childFeature2);
        IFeature childFeature3 = featureModel.mutate().addFeature("Test3");
        IFeatureTree childTree3 = childTree1.mutate().addFeatureBelow(childFeature3);

        return rootTree;
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

}
