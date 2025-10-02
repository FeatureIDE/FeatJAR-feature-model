/*
 * Copyright (C) 2025 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-feature-model.
 *
 * feature-model is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * feature-model is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with feature-model. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-feature-model> for further information.
 */
package de.featjar.feature.model.analysis;

import static org.junit.jupiter.api.Assertions.*;

import de.featjar.Common;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureTree;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

public class SimpleTreePropertiesTest extends Common {
    SimpleTreeProperties simpleTreeProperties = new SimpleTreeProperties();
    IFeatureTree smallTree = generateSmallTree();
    IFeatureTree featureTestTree = generateFeatureTestTree();
    IFeatureTree mediumTree = generateMediumTree();

    /**
     * Creates a tree with a root node that has 1 child, and this child has 2 more children. The root starts
     * an alternative group
     * @return a small feature tree for testing purposes
     */
    private IFeatureTree generateSmallTree() {
        FeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
        IFeatureTree rootTree =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
        rootTree.mutate().toAlternativeGroup();

        IFeature childFeature1 = featureModel.mutate().addFeature("Root's Child (in AltGroup)");
        IFeatureTree childTree1 = rootTree.mutate().addFeatureBelow(childFeature1);
        IFeature childFeature2 = featureModel.mutate().addFeature("1st Child of Root's Child");
        childTree1.mutate().addFeatureBelow(childFeature2);
        IFeature childFeature3 = featureModel.mutate().addFeature("2nd Child of Root's Child");
        childTree1.mutate().addFeatureBelow(childFeature3);

        return rootTree;
    }

    // stolen from a predefined test
    private IFeatureTree generateFeatureTestTree() {
        FeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());

        // features
        IFeatureTree rootTree =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
        rootTree.mutate().toAndGroup();

        IFeature childFeature1 = featureModel.mutate().addFeature("Test1");
        IFeatureTree childTree1 = rootTree.mutate().addFeatureBelow(childFeature1);

        IFeature childFeature2 = featureModel.mutate().addFeature("Test2");
        IFeatureTree childTree2 = rootTree.mutate().addFeatureBelow(childFeature2);

        IFeature childFeature3 = featureModel.mutate().addFeature("Test3");
        IFeatureTree childTree3 = childTree1.mutate().addFeatureBelow(childFeature3);
        childTree1.mutate().toAlternativeGroup();

        IFeature childFeature4 = featureModel.mutate().addFeature("Test4");
        childTree1.mutate().addFeatureBelow(childFeature4);

        IFeature childFeature5 = featureModel.mutate().addFeature("Test5");
        childTree2.mutate().addFeatureBelow(childFeature5);
        childTree2.mutate().toOrGroup();

        IFeature childFeature6 = featureModel.mutate().addFeature("Test6");
        childTree2.mutate().addFeatureBelow(childFeature6);

        IFeature childFeature7 = featureModel.mutate().addFeature("Test7");
        IFeatureTree childTree7 = rootTree.mutate().addFeatureBelow(childFeature7);
        childTree7.mutate().makeMandatory();

        IFeature childFeature8 = featureModel.mutate().addFeature("Test8");
        childTree3.mutate().addFeatureBelow(childFeature8);

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

        IFeature featureGet = featureModel.mutate().addFeature("Get");
        treeAPI.mutate().addFeatureBelow(featureGet);
        IFeature featurePut = featureModel.mutate().addFeature("Put");
        treeAPI.mutate().addFeatureBelow(featurePut);
        IFeature featureDelete = featureModel.mutate().addFeature("Delete");
        treeAPI.mutate().addFeatureBelow(featureDelete);
        treeAPI.mutate().toOrGroup();

        IFeature featureTransactions = featureModel.mutate().addFeature("Transactions");
        IFeatureTree treeTransactions = treeRoot.mutate().addFeatureBelow(featureTransactions);
        treeTransactions.isOptional();

        IFeature featureOS = featureModel.mutate().addFeature("OS");
        IFeatureTree treeOS = treeRoot.mutate().addFeatureBelow(featureOS);
        treeOS.isMandatory();

        IFeature featureWindows = featureModel.mutate().addFeature("Windows");
        treeOS.mutate().addFeatureBelow(featureWindows);
        IFeature featureLinux = featureModel.mutate().addFeature("Linux");
        treeOS.mutate().addFeatureBelow(featureLinux);
        treeOS.mutate().toAlternativeGroup();

        return treeRoot;
    }

    @Test
    void testTopFeatures() {
        int rootChildren = simpleTreeProperties.topFeatures(smallTree).get();
        assertEquals(1, rootChildren);

        rootChildren = simpleTreeProperties.topFeatures(mediumTree).get();
        assertEquals(3, rootChildren);
    }

    @Test
    void testLeafFeaturesCounter() {
        int leaves = simpleTreeProperties.leafFeaturesCounter(smallTree).get();
        assertEquals(2, leaves);

        leaves = simpleTreeProperties.leafFeaturesCounter(mediumTree).get();
        assertEquals(6, leaves);
    }

    @Test
    void testTreeDepth() {
        int depth = simpleTreeProperties.treeDepth(smallTree).get();
        assertEquals(3, depth);

        depth = simpleTreeProperties.treeDepth(mediumTree).get();
        assertEquals(3, depth);
    }

    @Test
    void testAvgNumberOfChildren() {
        float average = simpleTreeProperties.avgNumberOfChildren(smallTree).get();
        assertEquals(0.75, average);

        average = simpleTreeProperties.avgNumberOfChildren(mediumTree).get();
        assertTrue(0.888 < average && average < 0.889);
    }

    @Test
    void testGroupDistribution() {
        HashMap<String, Integer> groupCounts =
                simpleTreeProperties.groupDistribution(smallTree).get();
        assertEquals(1, groupCounts.get("AlternativeGroup"));
        assertEquals(3, groupCounts.get("AndGroup"));
        assertEquals(0, groupCounts.get("OrGroup"));

        groupCounts = simpleTreeProperties.groupDistribution(mediumTree).get();
        assertEquals(1, groupCounts.get("AlternativeGroup"));
        assertEquals(7, groupCounts.get("AndGroup"));
        assertEquals(1, groupCounts.get("OrGroup"));
    }

    // temp test regarding and groups
    @Test
    void mediumTest() {
        IFeatureTree tree = featureTestTree;
        HashMap<String, Integer> groupCounts =
                simpleTreeProperties.groupDistribution(tree).get();
        System.out.println(groupCounts);

        IFeatureTree tree2 = generateMediumTree();
        HashMap<String, Integer> groupCounts2 =
                simpleTreeProperties.groupDistribution(tree2).get();
        System.out.println(groupCounts2);
    }

    // temp test regarding and groups
    @Test
    void minimalAndGroupTest() {
        FeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
        IFeatureTree tree =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
        HashMap<String, Integer> groupCounts =
                simpleTreeProperties.groupDistribution(tree).get();
        System.out.println(groupCounts);
    }
}
