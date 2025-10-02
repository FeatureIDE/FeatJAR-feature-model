package de.featjar.feature.model.analysis;

import de.featjar.base.data.Result;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.base.tree.Trees;
import de.featjar.base.tree.visitor.TreeDepthCounter;
import de.featjar.feature.model.*;
import de.featjar.feature.model.analysis.visitor.TreeAvgChildrenCounter;
import de.featjar.feature.model.analysis.visitor.TreeLeafCounter;

import java.util.List;

public class SimpleTreeProperties {

    /**
     * Automatically finds the root of the given subtree.
     * @param tree: feature tree (whose root will be found automatically)
     * @return number of features directly below the root of this tree.
     */
    public Result<Integer> topFeatures(IFeatureTree tree) {
        int childrenCount = tree.getRoot().getChildrenCount();
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
        TreeDepthCounter visitor = new TreeDepthCounter();
        return Trees.traverse(tree,visitor);
    }

    /**
     * @param tree: feature tree
     * @return average number of children that each node in the tree has, rounded to integer.
     */
    public Result<Integer> avgNumberOfChildren(IFeatureTree tree) {
        TreeAvgChildrenCounter visitor = new TreeAvgChildrenCounter();
        return Trees.traverse(tree,visitor);
    }

    // work in progress
    public void groupDistribution() {
        // build a sample tree
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

        // check subtree for groups
        List<FeatureTree.Group> children = rootTree.getChildrenGroups();
        for (FeatureTree.Group child : children) {
            boolean isAnd = child.isAlternative();
            System.out.println(isAnd);
        }

    }

    public static void main(String[] args){
        // still have to make all the functions return Results, not ints
        // still have to write docs
        SimpleTreeProperties simpleTreeProperties = new SimpleTreeProperties();
        simpleTreeProperties.groupDistribution();

    }
}
