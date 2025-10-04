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
    IFeatureTree minimalTree = generateMinimalTree();
    IFeatureTree smallTree = generateSmallTree();
    IFeatureTree mediumTree = generateMediumTree();

    /**
     * @return bare-bones feature tree with just a root node to test edge cases.
     */
    private IFeatureTree generateMinimalTree() {
        FeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
        return featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
    }

    /**
     * Creates a feature tree with a root node that has 1 child, and this child has 2 more children. The root starts
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

    /**
     * Feature tree with three nodes under the root. API is mandatory and below it is an or-group with the features
     * Get, Put, Delete. OS is also mandatory and below it is an alternative group with the features Windows, Linux.
     * Transactions is an optional feature below the root.
     * @return a medium-sized feature tree for testing purposes.
     */
    private IFeatureTree generateMediumTree() {
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

        return treeRoot;
    }

    @Test
    void testTopFeatures() {
        int rootChildren;

        rootChildren = simpleTreeProperties.topFeatures(minimalTree).get();
        assertEquals(0, rootChildren);

        rootChildren = simpleTreeProperties.topFeatures(smallTree).get();
        assertEquals(1, rootChildren);

        rootChildren = simpleTreeProperties.topFeatures(mediumTree).get();
        assertEquals(3, rootChildren);
    }

    @Test
    void testLeafFeaturesCounter() {
        int leaves;

        leaves = simpleTreeProperties.leafFeaturesCounter(minimalTree).get();
        assertEquals(1, leaves);

        leaves = simpleTreeProperties.leafFeaturesCounter(smallTree).get();
        assertEquals(2, leaves);

        leaves = simpleTreeProperties.leafFeaturesCounter(mediumTree).get();
        assertEquals(6, leaves);
    }

    @Test
    void testTreeDepth() {
        int depth;

        depth = simpleTreeProperties.treeDepth(minimalTree).get();
        assertEquals(1, depth);

        depth = simpleTreeProperties.treeDepth(smallTree).get();
        assertEquals(3, depth);

        depth = simpleTreeProperties.treeDepth(mediumTree).get();
        assertEquals(3, depth);
    }

    @Test
    void testAvgNumberOfChildren() {
        float average;

        average = simpleTreeProperties.avgNumberOfChildren(minimalTree).get();
        assertEquals(0.0, average);

        average = simpleTreeProperties.avgNumberOfChildren(smallTree).get();
        assertEquals(0.75, average);

        average = simpleTreeProperties.avgNumberOfChildren(mediumTree).get();
        assertTrue(0.888 < average && average < 0.889);
    }

    @Test
    void testGroupDistribution() {
        HashMap<String, Integer> groupCounts;

        groupCounts = simpleTreeProperties.groupDistribution(minimalTree).get();
        assertEquals(0, groupCounts.get("AlternativeGroup"));
        assertEquals(1, groupCounts.get("AndGroup"));
        assertEquals(0, groupCounts.get("OrGroup"));

        groupCounts = simpleTreeProperties.groupDistribution(smallTree).get();
        assertEquals(1, groupCounts.get("AlternativeGroup"));
        assertEquals(3, groupCounts.get("AndGroup"));
        assertEquals(0, groupCounts.get("OrGroup"));

        groupCounts = simpleTreeProperties.groupDistribution(mediumTree).get();
        assertEquals(1, groupCounts.get("AlternativeGroup"));
        assertEquals(7, groupCounts.get("AndGroup"));
        assertEquals(1, groupCounts.get("OrGroup"));
    }
}
