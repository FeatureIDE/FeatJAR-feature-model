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
     * {@return number of features directly below the root of this subtree.}
     */
    public Result<Integer> topFeatures(IFeatureTree tree) {
        int childrenCount = tree.getChildrenCount(); // when doing computations: input the feature model
        return Result.of(childrenCount);
    }

    /**
     * @param tree: feature tree
     * {@return the number of features that have no child features}
     */
    public Result<Integer> leafFeaturesCounter(IFeatureTree tree) {
        return Trees.traverse(tree, new TreeLeafCounter());
    }

    /**
     * @param tree: feature tree
     * {@return tree depth, meaning the longest path from this subtree's root to its most distant leaf node}
     */
    public Result<Integer> treeDepth(IFeatureTree tree) {
        return Trees.traverse(tree, new TreeDepthCounter());
    }

    /**
     * @param tree: feature tree
     * {@return average number of children that each node in the tree has, rounded to integer.}
     */
    public Result<Double> avgNumberOfChildren(IFeatureTree tree) {
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
