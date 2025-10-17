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
import de.featjar.feature.model.TestDataProvider;
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
                        variableMap.get("API").get(),
                        variableMap.get("Get").get(),
                        variableMap.get("Windows").get(),
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
                        variableMap.get("Linux").get()),
                new BooleanAssignment(
                        variableMap.get("ConfigDB").get(),
                        -variableMap.get("API").get(),
                        variableMap.get("Get").get(),
                        variableMap.get("Windows").get(),
                        -variableMap.get("Put").get(),
                        -variableMap.get("Delete").get(),
                        -variableMap.get("Transactions").get(),
                        -variableMap.get("Linux").get()),
                new BooleanAssignment(
                        variableMap.get("ConfigDB").get(),
                        variableMap.get("API").get(),
                        variableMap.get("Get").get(),
                        variableMap.get("Windows").get(),
                        variableMap.get("Put").get(),
                        -variableMap.get("Delete").get(),
                        -variableMap.get("Transactions").get(),
                        -variableMap.get("Linux").get()));
        return booleanAssignmentList;
    }

    @Test
    public void computeDistributionFeaturesSelectionsTest() {
        BooleanAssignmentList booleanAssignmentList = createAssignmentList();
        IComputation<LinkedHashMap<String, Integer>> computational =
                Computations.of(booleanAssignmentList).map(ComputeDistributionFeatureSelections::new);
        HashMap<String, Integer> selectionDistribution = computational.compute();
        assertEquals(7, selectionDistribution.get("selected"));
        assertEquals(6, selectionDistribution.get("deselected"));
        assertEquals(22, selectionDistribution.get("undefined"));
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
    }

    @Test
    public void computeNumberConfigurationTest() {
        BooleanAssignmentList booleanAssignmentList = createAssignmentList();
        IComputation<Integer> computational =
                Computations.of(booleanAssignmentList).map(ComputeNumberConfigurations::new);
        assertEquals(5, computational.compute());
    }

    @Test
    public void computeNumberVariablesTest() {
        BooleanAssignmentList booleanAssignmentList = createAssignmentList();
        IComputation<Integer> computational =
                Computations.of(booleanAssignmentList).map(ComputeNumberVariables::new);
        assertEquals(7, computational.compute());
    }

    @Test
    public void computeUniformity() {
        FeatJAR.initialize();
        FeatureModel testFM = TestDataProvider.createMediumFeatureModel();
        IComputation<LinkedHashMap<String, Float>> computation = Computations.of(
                        (IFeatureModel) testFM)
                .map(ComputeUniformity::new)
                .set(
                        ComputeUniformity.BOOLEAN_ASSIGNMENT_LIST,
                        createAssignmentListUniformity(testFM))
                .set(ComputeUniformity.ANALYSIS, false);

        HashMap<String, Float> result = computation.compute();

        assertEquals(26, result.get("ConfigDB_FeatureModel_selected"));
        assertEquals(3, result.get("ConfigDB_AssignmentsSample_selected"));
        assertEquals(26, result.get("API_FeatureModel_selected"));
        assertEquals(2, result.get("API_AssignmentsSample_selected"));
        assertEquals(26, result.get("OS_FeatureModel_selected"));
        assertEquals(0, result.get("OS_AssignmentsSample_selected"));
        assertEquals(14, result.get("Get_FeatureModel_selected"));
        assertEquals(3, result.get("Get_AssignmentsSample_selected"));
        assertEquals(16, result.get("Put_FeatureModel_selected"));
        assertEquals(2, result.get("Put_AssignmentsSample_selected"));
        assertEquals(16, result.get("Delete_FeatureModel_selected"));
        assertEquals(1, result.get("Delete_AssignmentsSample_selected"));
        assertEquals(13, result.get("Windows_FeatureModel_selected"));
        assertEquals(2, result.get("Windows_AssignmentsSample_selected"));
        assertEquals(13, result.get("Linux_FeatureModel_selected"));
        assertEquals(1, result.get("Linux_AssignmentsSample_selected"));
        assertEquals(12, result.get("Transactions_FeatureModel_selected"));
        assertEquals(1, result.get("Transactions_AssignmentsSample_selected"));
        assertEquals(26, result.get("FeatureModel Valid"));
        assertEquals(3, result.get("AssignmentsSample Valid"));

        assertEquals(0, result.get("ConfigDB_FeatureModel_undefined"));
        assertEquals(0, result.get("ConfigDB_AssignmentsSample_undefined"));
        assertEquals(1, result.get("API_AssignmentsSample_undefined"));
        assertEquals(3, result.get("OS_AssignmentsSample_undefined"));
        assertEquals(0, result.get("Get_AssignmentsSample_undefined"));
        assertEquals(1, result.get("Put_AssignmentsSample_undefined"));
        assertEquals(0, result.get("Delete_AssignmentsSample_undefined"));
        assertEquals(0, result.get("Windows_AssignmentsSample_undefined"));
        assertEquals(0, result.get("Linux_AssignmentsSample_undefined"));
        assertEquals(0, result.get("Transactions_AssignmentsSample_undefined"));

        assertEquals(0, result.get("ConfigDB_FeatureModel_deselected"));
        assertEquals(0, result.get("ConfigDB_AssignmentsSample_deselected"));
        assertEquals(0, result.get("API_FeatureModel_deselected"));
        assertEquals(0, result.get("API_AssignmentsSample_deselected"));
        assertEquals(0, result.get("OS_FeatureModel_deselected"));
        assertEquals(0, result.get("OS_AssignmentsSample_deselected"));
        assertEquals(12, result.get("Get_FeatureModel_deselected"));
        assertEquals(0, result.get("Get_AssignmentsSample_deselected"));
        assertEquals(10, result.get("Put_FeatureModel_deselected"));
        assertEquals(0, result.get("Put_AssignmentsSample_deselected"));
        assertEquals(10, result.get("Delete_FeatureModel_deselected"));
        assertEquals(2, result.get("Delete_AssignmentsSample_deselected"));
        assertEquals(13, result.get("Windows_FeatureModel_deselected"));
        assertEquals(1, result.get("Windows_AssignmentsSample_deselected"));
        assertEquals(13, result.get("Linux_FeatureModel_deselected"));
        assertEquals(2, result.get("Linux_AssignmentsSample_deselected"));
        assertEquals(14, result.get("Transactions_FeatureModel_deselected"));
        assertEquals(2, result.get("Transactions_AssignmentsSample_deselected"));

        computation.set(ComputeUniformity.ANALYSIS, true);
        result = computation.compute();
        assertEquals(0, result.get("ConfigDB_selected"));
        assertEquals(((float) 2 / 3) - (float) 26 / (float) 26, result.get("API_selected"));
        assertEquals(-1, result.get("OS_selected"));
        assertEquals(((float) 3 / 3) - ((float) 14 / 26), result.get("Get_selected"));
        assertEquals(((float) 2 / 3) - ((float) 16 / 26), result.get("Put_selected"));
        assertEquals(((float) 1 / 3) - ((float) 16 / 26), result.get("Delete_selected"));
        assertEquals(((float) 2 / 3) - ((float) 13 / 26), result.get("Windows_selected"));
        assertEquals(((float) 1 / 3) - ((float) 13 / 26), result.get("Linux_selected"));
        assertEquals(((float) 1 / 3) - ((float) 12 / 26), result.get("Transactions_selected"));

        assertEquals(0, result.get("ConfigDB_deselected"));
        assertEquals(((float) 0 / 3) - (float) 0 / (float) 26, result.get("API_deselected"));
        assertEquals(0, result.get("OS_deselected"));
        assertEquals(((float) 0 / 3) - ((float) 12 / 26), result.get("Get_deselected"));
        assertEquals(((float) 0 / 3) - ((float) 10 / 26), result.get("Put_deselected"));
        assertEquals(((float) 2 / 3) - ((float) 10 / 26), result.get("Delete_deselected"));
        assertEquals(((float) 1 / 3) - ((float) 13 / 26), result.get("Windows_deselected"));
        assertEquals(((float) 2 / 3) - ((float) 13 / 26), result.get("Linux_deselected"));
        assertEquals(((float) 2 / 3) - ((float) 14 / 26), result.get("Transactions_deselected"));

        assertEquals(0, result.get("ConfigDB_undefined"));
        assertEquals(((float) 1 / 3) - (float) 0 / (float) 26, result.get("API_undefined"));
        assertEquals(1, result.get("OS_undefined"));
        assertEquals(((float) 0 / 3) - ((float) 0 / 26), result.get("Get_undefined"));
        assertEquals(((float) 1 / 3) - ((float) 0 / 26), result.get("Put_undefined"));
        assertEquals(((float) 0 / 3) - ((float) 0 / 26), result.get("Delete_undefined"));
        assertEquals(((float) 0 / 3) - ((float) 0 / 26), result.get("Windows_undefined"));
        assertEquals(((float) 0 / 3) - ((float) 0 / 26), result.get("Linux_undefined"));
        assertEquals(((float) 0 / 3) - ((float) 0 / 26), result.get("Transactions_undefined"));
        
        FeatJAR.deinitialize();
    }
}
