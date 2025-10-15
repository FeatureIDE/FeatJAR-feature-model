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

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.featjar.base.FeatJAR;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.IComputation;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.transformer.ComputeFormula;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.predicate.Literal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import org.junit.jupiter.api.Test;

public class SamplePropertiesTest {

    public BooleanAssignmentList createAssignmentList() {
        LinkedList<String> variableNames = new LinkedList<String>();
        variableNames.add("A");
        variableNames.add("B");
        variableNames.add("C");
        variableNames.add("D");
        variableNames.add("E");
        variableNames.add("F");
        variableNames.add("G");
        VariableMap variableMap = new VariableMap(variableNames);

        BooleanAssignmentList booleanAssignmentList = new BooleanAssignmentList(
                variableMap,
                new BooleanAssignment(1, -2, -5, -6),
                new BooleanAssignment(-1, -3, -6),
                new BooleanAssignment(1, 2, 4, 5),
                new BooleanAssignment(5, 6),
                new BooleanAssignment());
        return booleanAssignmentList;
    }

    public BooleanAssignmentList createAssignmentListUniformity(FeatureModel featureModel) {
        LinkedList<String> variableNames = new LinkedList<String>();
        IComputation<IFormula> iFormula =
                Computations.of((IFeatureModel) featureModel).map(ComputeFormula::new);
        IFormula fmFormula = iFormula.compute();
        VariableMap variableMap = new VariableMap(fmFormula);
        BooleanAssignmentList booleanAssignmentList = new BooleanAssignmentList(
                variableMap,
                new BooleanAssignment(
                        variableMap.get("ConfigDB").get(),
                        variableMap.get("Get").get(),
                        variableMap.get("Windows").get(),
                        -variableMap.get("Put").get(),
                        -variableMap.get("Delete").get(),
                        -variableMap.get("Transactions").get(),
                        -variableMap.get("Linux").get()),
                new BooleanAssignment(
                        variableMap.get("ConfigDB").get(),
                        variableMap.get("Get").get(),
                        -variableMap.get("Windows").get(),
                        variableMap.get("Put").get(),
                        variableMap.get("Delete").get(),
                        variableMap.get("Transactions").get(),
                        variableMap.get("Linux").get()),
                new BooleanAssignment(
                        variableMap.get("ConfigDB").get(),
                        variableMap.get("Get").get(),
                        -variableMap.get("Windows").get(),
                        -variableMap.get("Put").get(),
                        -variableMap.get("Delete").get(),
                        -variableMap.get("Transactions").get(),
                        -variableMap.get("Linux").get()),
                new BooleanAssignment(
                        variableMap.get("ConfigDB").get(),
                        variableMap.get("Get").get(),
                        -variableMap.get("Windows").get(),
                        -variableMap.get("Put").get(),
                        -variableMap.get("Delete").get(),
                        variableMap.get("Transactions").get(),
                        variableMap.get("Linux").get()));
        return booleanAssignmentList;
    }

    public FeatureModel createMediumFeatureModel() {
        FeatureModel fm = new FeatureModel();
        fm.addFeatureTreeRoot(generateMediumTree());
        fm.addConstraint(new Implies(new Literal("Transactions"), new Or(new Literal("Put"), new Literal("Delete"))));
        return fm;
    }

    @Test
    public void computeDistributionFeaturesSelectionsTest() {
        BooleanAssignmentList booleanAssignmentList = createAssignmentList();
        IComputation<HashMap<String, Integer>> computational =
                Computations.of(booleanAssignmentList).map(ComputeDistributionFeatureSelections::new);
        HashMap<String, Integer> selectionDistribution = computational.compute();
        assertEquals(7, selectionDistribution.get("selected"));
        assertEquals(6, selectionDistribution.get("deselected"));
        assertEquals(22, selectionDistribution.get("undefined"));
        System.out.println("Distribution of feature selection: \n" + selectionDistribution);
    }

    @Test
    public void computeFeatureCounterTest() {
        BooleanAssignmentList booleanAssignmentList = createAssignmentList();
        HashMap<String, Integer> featureCounter = Computations.of(booleanAssignmentList)
                .map(ComputeFeatureCounter::new)
                .compute();
        assertEquals(2, featureCounter.get("A_selected"));
        assertEquals(1, featureCounter.get("B_selected"));
        assertEquals(0, featureCounter.get("C_selected"));
        assertEquals(1, featureCounter.get("D_selected"));
        assertEquals(2, featureCounter.get("E_selected"));
        assertEquals(1, featureCounter.get("F_selected"));
        assertEquals(0, featureCounter.get("G_selected"));

        assertEquals(1, featureCounter.get("A_deselected"));
        assertEquals(1, featureCounter.get("B_deselected"));
        assertEquals(1, featureCounter.get("C_deselected"));
        assertEquals(0, featureCounter.get("D_deselected"));
        assertEquals(1, featureCounter.get("E_deselected"));
        assertEquals(2, featureCounter.get("F_deselected"));
        assertEquals(0, featureCounter.get("G_deselected"));

        assertEquals(2, featureCounter.get("A_undefined"));
        assertEquals(3, featureCounter.get("B_undefined"));
        assertEquals(4, featureCounter.get("C_undefined"));
        assertEquals(4, featureCounter.get("D_undefined"));
        assertEquals(2, featureCounter.get("E_undefined"));
        assertEquals(2, featureCounter.get("F_undefined"));
        assertEquals(5, featureCounter.get("G_undefined"));
        System.out.println("Distribution of feature selection per feature: \n" + featureCounter);
    }

    @Test
    public void computeNumberConfigurationTest() {
        BooleanAssignmentList booleanAssignmentList = createAssignmentList();
        IComputation<Integer> computational =
                Computations.of(booleanAssignmentList).map(ComputeNumberConfigurations::new);
        assertEquals(5, computational.compute());
        System.out.println("Number of configurations: \n" + computational.compute());
    }

    @Test
    public void computeNumberVariablesTest() {
        BooleanAssignmentList booleanAssignmentList = createAssignmentList();
        IComputation<Integer> computational =
                Computations.of(booleanAssignmentList).map(ComputeNumberVariables::new);
        assertEquals(7, computational.compute());
        System.out.println("Number of variables: \n" + computational.compute());
    }

    @Test
    public void computeUniformity() {
        FeatJAR.initialize();
        IComputation<LinkedHashMap<String, Float>> computation = Computations.of(
                        (IFeatureModel) createMediumFeatureModel())
                .map(ComputeUniformity::new)
                .set(
                        ComputeUniformity.BOOLEAN_ASSIGNMENT_LIST,
                        createAssignmentListUniformity(createMediumFeatureModel()))
                .set(ComputeUniformity.ANALYSIS, false);

        HashMap<String, Float> result = computation.compute();
        assertEquals(26, result.get("FeatureModel Valid"));
        assertEquals(2, result.get("AssignmentsSample Valid"));
        assertEquals(26, result.get("ConfigDB_FeatureModel"));
        assertEquals(2, result.get("ConfigDB_AssignmentsSample"));
        assertEquals(26, result.get("API_FeatureModel"));
        assertEquals(0, result.get("API_AssignmentsSample"));
        assertEquals(26, result.get("OS_FeatureModel"));
        assertEquals(0, result.get("OS_AssignmentsSample"));
        assertEquals(14, result.get("Get_FeatureModel"));
        assertEquals(2, result.get("Get_AssignmentsSample"));
        assertEquals(16, result.get("Put_FeatureModel"));
        assertEquals(1, result.get("Put_AssignmentsSample"));
        assertEquals(16, result.get("Delete_FeatureModel"));
        assertEquals(1, result.get("Delete_AssignmentsSample"));
        assertEquals(13, result.get("Windows_FeatureModel"));
        assertEquals(1, result.get("Windows_AssignmentsSample"));
        assertEquals(13, result.get("Linux_FeatureModel"));
        assertEquals(1, result.get("Linux_AssignmentsSample"));
        assertEquals(1, result.get("Windows_AssignmentsSample"));
        assertEquals(13, result.get("Linux_FeatureModel"));
        assertEquals(1, result.get("Linux_AssignmentsSample"));
        assertEquals(12, result.get("Transactions_FeatureModel"));
        assertEquals(1, result.get("Transactions_AssignmentsSample"));
        assertEquals(26, result.get("FeatureModel Valid"));
        assertEquals(2, result.get("AssignmentsSample Valid"));
        System.out.println("Descriptive validtiy map: \n" + result);

        computation.set(ComputeUniformity.ANALYSIS, true);
        result = computation.compute();
        assertEquals(0, result.get("ConfigDB"));
        assertEquals(-1, result.get("API"));
        assertEquals(-1, result.get("OS"));
        assertEquals(((float) 2 / 2) - ((float) 14 / 26), result.get("Get"));
        assertEquals(((float) 1 / 2) - ((float) 16 / 26), result.get("Put"));
        assertEquals(((float) 1 / 2) - ((float) 16 / 26), result.get("Delete"));
        assertEquals(0, result.get("Windows"));
        assertEquals(0, result.get("Linux"));
        assertEquals(((float) 1 / 2) - ((float) 12 / 26), result.get("Transactions"));
        System.out.println("Commonality difference per features: \n" + result);
    }

    private IFeatureTree generateMediumTree() {
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
}
