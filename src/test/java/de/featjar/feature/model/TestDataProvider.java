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

        IFeature featureTransactions = featureModel.mutate().addFeature("Transactions");
        IFeatureTree treeTransactions = treeRoot.mutate().addFeatureBelow(featureTransactions);
        treeTransactions.isOptional();

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
