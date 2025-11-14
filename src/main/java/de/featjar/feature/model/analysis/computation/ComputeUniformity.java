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
package de.featjar.feature.model.analysis.computation;

import de.featjar.analysis.javasmt.computation.ComputeSatisfiability;
import de.featjar.analysis.javasmt.computation.ComputeSolutionCount;
import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.transformer.ComputeFormula;
import de.featjar.formula.VariableMap;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.computation.ComputeCNFFormula;
import de.featjar.formula.computation.ComputeNNFFormula;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Not;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.Literal;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Computes how often features are selected compared to their general selection in their feature model.
 * This takes only valid configurations into account.
 *
 * BOOLEAN_ASSIGNMENT_LIST - current assignment
 * FEATURE_MODEL - feature model
 * ANALYSIS - when false return the full count of valid assignments in both AssignmentSample and the FeatureModel per feature,
 * else the difference between the commonality of the AssignmentSample and the FeatureModel per feature.
 *
 * @author Mohammad Khair Almekkawi
 * @author Florian Beese
 */
public class ComputeUniformity extends AComputation<LinkedHashMap<String, Float>> {

    public static final Dependency<BooleanAssignmentList> BOOLEAN_ASSIGNMENT_LIST =
            Dependency.newDependency(BooleanAssignmentList.class);
    public static final Dependency<IFeatureModel> FEATURE_MODEL = Dependency.newDependency(IFeatureModel.class);
    public static final Dependency<Boolean> ANALYSIS = Dependency.newDependency(Boolean.class);

    public ComputeUniformity(IComputation<IFeatureModel> featureModel) {
        super(
                Computations.of(new BooleanAssignmentList(new VariableMap())),
                featureModel,
                Computations.of(Boolean.TRUE));
    }

    @Override
    public Result<LinkedHashMap<String, Float>> compute(List<Object> dependencyList, Progress progress) {
        BooleanAssignmentList booleanAssignmentList = BOOLEAN_ASSIGNMENT_LIST.get(dependencyList);
        IFeatureModel featureModel = FEATURE_MODEL.get(dependencyList);
        IFormula fmFormula =
                Computations.of(featureModel).map(ComputeFormula::new).compute();
        float solutionsCount = Computations.of(fmFormula)
                .map(ComputeNNFFormula::new)
                .map(ComputeCNFFormula::new)
                .map(ComputeSolutionCount::new)
                .compute()
                .floatValue();
        LinkedHashMap<String, Float> returnedMap = new LinkedHashMap<String, Float>();
        VariableMap fmVariableMap = new VariableMap(fmFormula);
        String featureModelPrefix = "_FeatureModel";
        String assignmentsSamplePrefix = "_AssignmentsSample";

        for (String varName : fmVariableMap.getVariableNames()) {
            returnedMap.put(varName + featureModelPrefix + "_selected", (float) 0);
            returnedMap.put(varName + assignmentsSamplePrefix + "_selected", (float) 0);
            returnedMap.put(varName + featureModelPrefix + "_deselected", (float) 0);
            returnedMap.put(varName + assignmentsSamplePrefix + "_deselected", (float) 0);
            returnedMap.put(varName + featureModelPrefix + "_undefined", (float) 0);
            returnedMap.put(varName + assignmentsSamplePrefix + "_undefined", (float) 0);
        }

        // Calculate the number of valid configurations per feature in the full featureModel.
        for (String varName : fmVariableMap.getVariableNames()) {
            Reference currentFormula =
                    new Reference(new And((IFormula) fmFormula.getChildren().get(0), new Literal(varName)));
            currentFormula.setFreeVariables(((Reference) fmFormula).getFreeVariables());
            IFormula NNFFormula = Computations.of((IFormula) currentFormula)
                    .map(ComputeNNFFormula::new)
                    .compute();
            returnedMap.replace(
                    varName + featureModelPrefix + "_selected",
                    Computations.of(NNFFormula)
                            .map(ComputeCNFFormula::new)
                            .map(ComputeSolutionCount::new)
                            .compute()
                            .floatValue());
        }

        // Calculate the number of valid configurations per feature in the full featureModel.
        for (String varName : fmVariableMap.getVariableNames()) {
            Reference currentFormula =
                    new Reference(new And((IFormula) fmFormula.getChildren().get(0), new Not(new Literal(varName))));
            currentFormula.setFreeVariables(((Reference) fmFormula).getFreeVariables());
            IFormula NNFFormula = Computations.of((IFormula) currentFormula)
                    .map(ComputeNNFFormula::new)
                    .compute();
            returnedMap.replace(
                    varName + featureModelPrefix + "_deselected",
                    Computations.of(NNFFormula)
                            .map(ComputeCNFFormula::new)
                            .map(ComputeSolutionCount::new)
                            .compute()
                            .floatValue());
        }

        // Calculate the number of valid configurations per feature in the full assignmentSample.
        int assignmentSolutionsCount = 0;
        for (BooleanAssignment booleanAssignment : booleanAssignmentList.getAll()) {
            // save all Literals, Whether they are true and false, then add them to full formula with an And.
            LinkedList<IFormula> allLiterals = new LinkedList<IFormula>();
            // save the Name of the literals that are set to true/selected in the current assignment
            List<String> currentSelectedAssignmentVariables = new LinkedList<String>();
            // save the Name of the literals that are set to false/deselected in the current assignment
            List<String> currentDeselectedAssignmentVariables = new LinkedList<String>();
            for (int index : booleanAssignment.get()) {
                // Add selected Literal
                if (fmVariableMap.get(index).isPresent()) {
                    allLiterals.add(new Literal(fmVariableMap.get(index).get()));
                    currentSelectedAssignmentVariables.add(
                            fmVariableMap.get(index).get());
                    // Add deselected Literal
                } else if (fmVariableMap.get(Math.abs(index)).isPresent()) {
                    currentDeselectedAssignmentVariables.add(
                            fmVariableMap.get(Math.abs(index)).get());
                    allLiterals.add(new Not(
                            new Literal(fmVariableMap.get(Math.abs(index)).get())));
                    // shouldn't happen but just in case.
                } else {
                    Result.empty();
                }
            }
            IFormula currentIFormulaAssignment = new And(allLiterals);
            Reference currentFormula =
                    new Reference(new And((IFormula) fmFormula.getChildren().get(0), currentIFormulaAssignment));
            currentFormula.setFreeVariables(((Reference) fmFormula).getFreeVariables());
            // check if the formula is valid, if yes increase the count of the selected or deselected Literals in
            // returnedMap.
            if (Computations.of((IFormula) currentFormula)
                    .map(ComputeNNFFormula::new)
                    .map(ComputeCNFFormula::new)
                    .map(ComputeSatisfiability::new)
                    .compute()) {
                assignmentSolutionsCount++;
                for (String key : currentSelectedAssignmentVariables) {
                    returnedMap.replace(
                            key + assignmentsSamplePrefix + "_selected",
                            returnedMap.get(key + assignmentsSamplePrefix + "_selected") + 1);
                }

                for (String key : currentDeselectedAssignmentVariables) {
                    returnedMap.replace(
                            key + assignmentsSamplePrefix + "_deselected",
                            returnedMap.get(key + assignmentsSamplePrefix + "_deselected") + 1);
                }
            }
        }

        // For valid assignment calculate and add the count of undefined Literals in the AssignmentLists
        for (String varName : fmVariableMap.getVariableNames()) {
            returnedMap.replace(
                    varName + assignmentsSamplePrefix + "_undefined",
                    assignmentSolutionsCount
                            - returnedMap.get(varName + assignmentsSamplePrefix + "_selected")
                            - returnedMap.get(varName + assignmentsSamplePrefix + "_deselected"));
            returnedMap.replace(
                    varName + featureModelPrefix + "_undefined",
                    solutionsCount
                            - returnedMap.get(varName + featureModelPrefix + "_selected")
                            - returnedMap.get(varName + featureModelPrefix + "_deselected"));
        }

        if (ANALYSIS.get(dependencyList)) {
            for (String varName : fmVariableMap.getVariableNames()) {
                float sampleShareSelected = 0;
                float sampleShareDeselected = 0;
                float sampleShareUndefined = 0;
                float featureShareSelected = 0;
                float featureShareDeselected = 0;
                float featureShareUndefined = 0;

                if (assignmentSolutionsCount > 0) {
                    sampleShareSelected =
                            returnedMap.get(varName + assignmentsSamplePrefix + "_selected") / assignmentSolutionsCount;
                    sampleShareDeselected = returnedMap.get(varName + assignmentsSamplePrefix + "_deselected")
                            / assignmentSolutionsCount;
                    sampleShareUndefined = returnedMap.get(varName + assignmentsSamplePrefix + "_undefined")
                            / assignmentSolutionsCount;
                }

                if (solutionsCount > 0) {
                    featureShareSelected = returnedMap.get(varName + featureModelPrefix + "_selected") / solutionsCount;
                    featureShareDeselected =
                            returnedMap.get(varName + featureModelPrefix + "_deselected") / solutionsCount;
                    featureShareUndefined =
                            returnedMap.get(varName + featureModelPrefix + "_undefined") / solutionsCount;
                }

                returnedMap.remove(varName + assignmentsSamplePrefix + "_selected");
                returnedMap.remove(varName + featureModelPrefix + "_selected");
                returnedMap.remove(varName + assignmentsSamplePrefix + "_deselected");
                returnedMap.remove(varName + featureModelPrefix + "_deselected");
                returnedMap.remove(varName + assignmentsSamplePrefix + "_undefined");
                returnedMap.remove(varName + featureModelPrefix + "_undefined");
                returnedMap.put(varName + "_selected", sampleShareSelected - featureShareSelected);
                returnedMap.put(varName + "_deselected", sampleShareDeselected - featureShareDeselected);
                returnedMap.put(varName + "_undefined", sampleShareUndefined - featureShareUndefined);
            }
        } else {
            returnedMap.put("FeatureModel Valid", solutionsCount);
            returnedMap.put("AssignmentsSample Valid", (float) assignmentSolutionsCount);
        }
        return Result.of(returnedMap);
    }
}
