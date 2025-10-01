package de.featjar.feature.model.analysis;

import de.featjar.base.data.Result;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.base.tree.Trees;
import de.featjar.base.tree.visitor.TreeDepthCounter;
import de.featjar.feature.model.*;

public class SimpleTreeProperties {

    // todo
    public int topFeatures(IFeatureTree tree) {

        return tree.getRoot().getChildrenCount(); // do groups count as features?
    }

    public int leafFeatures(IFeatureTree tree) {
        return 0;
    }

    public int treeDepth(IFeatureTree tree) {
        TreeDepthCounter visitor = new TreeDepthCounter();
        Result<Integer> result = Trees.traverse(tree.getRoot(),visitor);
        return result.get();
    }

    public void testMethode() {
        FeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
        IFeatureTree rootTree =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
        //rootTree.mutate().toAndGroup();
        IFeature childFeature1 = featureModel.mutate().addFeature("Test1");
        IFeatureTree childTree1 = rootTree.mutate().addFeatureBelow(childFeature1);

        IFeature childFeature2 = featureModel.mutate().addFeature("Test2");
        IFeatureTree childTree2 = childTree1.mutate().addFeatureBelow(childFeature2);

        /*
        TreePrinter visitor = new TreePrinter();
        Result<String> traverseResult = Trees.traverse(rootTree, visitor);
        System.out.println(traverseResult.get());
         */

        int depth = treeDepth(rootTree);
        System.out.println(depth);


    }

    public static void main(String[] args){

        SimpleTreeProperties simpleTreeProperties = new SimpleTreeProperties();
        simpleTreeProperties.testMethode();

    }

}
