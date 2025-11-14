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

/**
 * Compute how often features are selected, deselected, or undefined.
 *
 * @author Mohammad Khair Almekkawi
 * @author Florian Beese
 */
public class ComputeDistributionFeatureSelections extends AComputation<LinkedHashMap<String, Integer>> {

    public static final Dependency<BooleanAssignmentList> BOOLEAN_ASSIGNMENT_LIST =
            Dependency.newDependency(BooleanAssignmentList.class);

    public ComputeDistributionFeatureSelections(IComputation<BooleanAssignmentList> booleanAssignmentList) {
        super(booleanAssignmentList);
    }

    @Override
    public Result<LinkedHashMap<String, Integer>> compute(List<Object> dependencyList, Progress progress) {
        BooleanAssignmentList booleanAssigmenAssignmentList = BOOLEAN_ASSIGNMENT_LIST.get(dependencyList);
        LinkedHashMap<String, Integer> selectionDistribution = new LinkedHashMap<String, Integer>();
        selectionDistribution.put("selected", 0);
        selectionDistribution.put("deselected", 0);
        selectionDistribution.put("undefined", 0);

        for (BooleanAssignment assignment : booleanAssigmenAssignmentList.getAll()) {
            selectionDistribution.replace(
                    "deselected", assignment.countNegatives() + selectionDistribution.get("deselected"));
            selectionDistribution.replace(
                    "selected", assignment.countPositives() + selectionDistribution.get("selected"));
            selectionDistribution.replace(
                    "undefined",
                    -assignment.countNonZero()
                            + booleanAssigmenAssignmentList
                                    .getVariableMap()
                                    .getVariableNames()
                                    .size()
                            + selectionDistribution.get("undefined"));
        }
        return Result.of(selectionDistribution);
    }
}
