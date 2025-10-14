/*
 * Copyright (C) 2025 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula.
 *
 * formula is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula> for further information.
 */
package de.featjar.feature.model.analysis;

import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.assignment.BooleanAssignment;
import de.featjar.formula.assignment.BooleanAssignmentList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComputeFeatureCounter extends AComputation<HashMap<String, HashMap<String, Integer>>> {

    protected static final Dependency<BooleanAssignmentList> BOOLEAN_ASSIGMENT_LIST =
            Dependency.newDependency(BooleanAssignmentList.class);

    public ComputeFeatureCounter(IComputation<BooleanAssignmentList> booleanAssigmentList) {
        super(booleanAssigmentList);
    }

    @Override
    public Result<HashMap<String, HashMap<String, Integer>>> compute(List<Object> dependencyList, Progress progress) {
        BooleanAssignmentList booleanAssignmentList = BOOLEAN_ASSIGMENT_LIST.get(dependencyList);
        HashMap<Integer, HashMap<String, Integer>> featureCounter = new HashMap<Integer, HashMap<String, Integer>>();

        // for(String feature : booleanAssignmentList.getVariableMap().getVariableNames()) {
        //	featureCounter.put(feature,0);
        // }

        for (int index : booleanAssignmentList.getVariableMap().getIndices()) {
            HashMap<String, Integer> tempMap = new HashMap<String, Integer>();
            tempMap.put("selected", 0);
            tempMap.put("deselected", 0);
            tempMap.put("undefined", 0);
            featureCounter.put(index, tempMap);
        }

        for (BooleanAssignment assignment : booleanAssignmentList.getAll()) {
            for (int index : booleanAssignmentList.getVariableMap().getIndices()) {
                if (assignment.contains(index)) {
                    featureCounter
                            .get(index)
                            .replace("selected", featureCounter.get(index).get("selected") + 1);
                } else if (assignment.containsAnyNegated(index)) {
                    featureCounter
                            .get(index)
                            .replace("deselected", featureCounter.get(index).get("deselected") + 1);
                } else {
                    featureCounter
                            .get(index)
                            .replace("undefined", featureCounter.get(index).get("undefined") + 1);
                }
            }
        }

        HashMap<String, HashMap<String, Integer>> featureNameCounter = new HashMap<String, HashMap<String, Integer>>();

        for (Map.Entry<Integer, HashMap<String, Integer>> entry : featureCounter.entrySet()) {
            if (booleanAssignmentList.getVariableMap().get(entry.getKey()).isPresent()) {
                featureNameCounter.put(
                        booleanAssignmentList
                                .getVariableMap()
                                .get(entry.getKey())
                                .get(),
                        entry.getValue());
            }
        }

        return Result.of(featureNameCounter);
    }
}
