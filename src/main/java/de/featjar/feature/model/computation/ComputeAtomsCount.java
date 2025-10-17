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
package de.featjar.feature.model.computation;

import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.base.tree.Trees;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IConstraint;
import de.featjar.feature.model.analysis.visitor.AtomsCountTreeVisitor;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Call the visitor AtomsCount on all constraints of a feature model to count the terminal expressions.
 * It is possible to set the three variables COUNTCONSTANTS, COUNTVARIABLES and COUNTBOOLEAN in order
 * to set what should be counted
 * For further information on its methods see {@link IComputation}
 *
 * @author Mohammad Khair Almekkawi
 * @author Florian Beese
 * */
public class ComputeAtomsCount extends AComputation<Integer> {
    public static final Dependency<FeatureModel> FEATUREMODEL = Dependency.newDependency(FeatureModel.class);
    // COUNTCONSTANTS decide if Atoms of type constants should be counted
    public static final Dependency<Boolean> COUNTCONSTANTS = Dependency.newDependency(Boolean.class);
    // COUNTVARIABLES decide if Atoms of type variable should be counted
    public static final Dependency<Boolean> COUNTVARIABLES = Dependency.newDependency(Boolean.class);
    // COUNTBOOLEAN decide if Atoms of type True or False should be counted
    public static final Dependency<Boolean> COUNTBOOLEAN = Dependency.newDependency(Boolean.class);

    public ComputeAtomsCount(IComputation<FeatureModel> featureModel) {
        super(
                featureModel,
                Computations.of(Boolean.TRUE),
                Computations.of(Boolean.TRUE),
                Computations.of(Boolean.TRUE));
    }

    @Override
    public Result<Integer> compute(List<Object> dependencyList, Progress progress) {
        Collection<IConstraint> Constraints = FEATUREMODEL.get(dependencyList).getConstraints();
        int atomsSum = 0;

        Iterator<IConstraint> constraintIterator = Constraints.iterator();
        while (constraintIterator.hasNext()) {
            atomsSum = atomsSum
                    + Trees.traverse(
                                    constraintIterator.next().getFormula(),
                                    new AtomsCountTreeVisitor(
                                            COUNTVARIABLES.get(dependencyList),
                                            COUNTCONSTANTS.get(dependencyList),
                                            COUNTBOOLEAN.get(dependencyList)))
                            .orElse(0);
        }

        return Result.of(atomsSum);
    }
}
