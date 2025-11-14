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

import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Compute how often features are selected, deselected, or undefined per feature.
 *
 * @author Mohammad Khair Almekkawi
 * @author Florian Beese
 */
public class ComputeFeatureCounter extends AComputation<LinkedHashMap<String, Integer>> {

    protected static final Dependency<BooleanAssignmentList> BOOLEAN_ASSIGMENT_LIST =
            Dependency.newDependency(BooleanAssignmentList.class);

    public ComputeFeatureCounter(IComputation<BooleanAssignmentList> booleanAssigmentList) {
        super(booleanAssigmentList);
    }

    @Override
    public Result<LinkedHashMap<String, Integer>> compute(List<Object> dependencyList, Progress progress) {
        BooleanAssignmentList booleanAssignmentList = BOOLEAN_ASSIGMENT_LIST.get(dependencyList);
        LinkedHashMap<String, Integer> featureCounter = new LinkedHashMap<String, Integer>();

        for (int index : booleanAssignmentList.getVariableMap().getIndices()) {
            featureCounter.put(index + "_selected", 0);
            featureCounter.put(index + "_deselected", 0);
            featureCounter.put(index + "_undefined", 0);
        }

        for (BooleanAssignment assignment : booleanAssignmentList.getAll()) {
            for (int index : booleanAssignmentList.getVariableMap().getIndices()) {
                if (assignment.contains(index)) {
                    featureCounter.replace(index + "_selected", featureCounter.get(index + "_selected") + 1);
                } else if (assignment.containsAnyNegated(index)) {
                    featureCounter.replace(index + "_deselected", featureCounter.get(index + "_deselected") + 1);
                } else {
                    featureCounter.replace(index + "_undefined", featureCounter.get(index + "_undefined") + 1);
                }
            }
        }

        LinkedHashMap<String, Integer> featureNameCounter = new LinkedHashMap<String, Integer>();

        for (Map.Entry<String, Integer> entry : featureCounter.entrySet()) {
            if (entry.getKey().contains("_selected")
                    && booleanAssignmentList
                            .getVariableMap()
                            .get(Integer.valueOf(entry.getKey().replace("_selected", "")))
                            .isPresent()) {
                featureNameCounter.put(
                        booleanAssignmentList
                                        .getVariableMap()
                                        .get(Integer.valueOf(entry.getKey().replace("_selected", "")))
                                        .get()
                                + "_selected",
                        entry.getValue());
            } else if (entry.getKey().contains("_deselected")
                    && booleanAssignmentList
                            .getVariableMap()
                            .get(Integer.valueOf(entry.getKey().replace("_deselected", "")))
                            .isPresent()) {
                featureNameCounter.put(
                        booleanAssignmentList
                                        .getVariableMap()
                                        .get(Integer.valueOf(entry.getKey().replace("_deselected", "")))
                                        .get()
                                + "_deselected",
                        entry.getValue());
            } else if (entry.getKey().contains("_undefined")
                    && booleanAssignmentList
                            .getVariableMap()
                            .get(Integer.valueOf(entry.getKey().replace("_undefined", "")))
                            .isPresent()) {
                featureNameCounter.put(
                        booleanAssignmentList
                                        .getVariableMap()
                                        .get(Integer.valueOf(entry.getKey().replace("_undefined", "")))
                                        .get()
                                + "_undefined",
                        entry.getValue());
            }
        }
        return Result.of(featureNameCounter);
    }
}
