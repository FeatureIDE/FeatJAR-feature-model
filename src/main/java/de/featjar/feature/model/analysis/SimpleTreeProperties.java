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

import de.featjar.base.data.Result;
import de.featjar.base.tree.Trees;
import de.featjar.base.tree.visitor.TreeDepthCounter;
import de.featjar.feature.model.*;
import de.featjar.feature.model.analysis.visitor.FeatureTreeGroupCounter;
import de.featjar.feature.model.analysis.visitor.TreeAvgChildrenCounter;
import de.featjar.feature.model.analysis.visitor.TreeLeafCounter;
import java.util.HashMap;

public class SimpleTreeProperties {

    /**
     * @param tree: feature tree
     * @return number of features directly below the root of this subtree.
     */
    public Result<Integer> topFeatures(IFeatureTree tree) {
        // int childrenCount = tree.getRoot().getChildrenCount(); // if we should find a subtree's root automatically
        int childrenCount = tree.getChildrenCount();
        return Result.of(childrenCount);
    }

    /**
     * @param tree: feature tree
     * @return the number of features that have no child features
     */
    public Result<Integer> leafFeaturesCounter(IFeatureTree tree) {
        return Trees.traverse(tree, new TreeLeafCounter());
    }

    /**
     * @param tree: feature tree
     * @return tree depth, meaning the longest path from this subtree's root to its most distant leaf node
     */
    public Result<Integer> treeDepth(IFeatureTree tree) {
        return Trees.traverse(tree, new TreeDepthCounter());
    }

    /**
     * @param tree: feature tree
     * @return average number of children that each node in the tree has, rounded to integer.
     */
    public Result<Float> avgNumberOfChildren(IFeatureTree tree) {
        return Trees.traverse(tree, new TreeAvgChildrenCounter());
    }

    /** Counts the number of different groups in this tree.
     * @param tree: feature tree
     * @return hashmap with the String keys "AlternativeGroup", "OrGroup" and "AndGroup" to get the respective counts
     */
    public Result<HashMap<String, Integer>> groupDistribution(IFeatureTree tree) {
        return Trees.traverse(tree, new FeatureTreeGroupCounter());
    }
}
