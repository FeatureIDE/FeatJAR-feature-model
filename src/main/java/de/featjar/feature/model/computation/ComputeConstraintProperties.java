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

import de.featjar.base.computation.*;
import de.featjar.base.computation.AComputation;
import de.featjar.base.data.Result;
import de.featjar.base.tree.Trees;
import de.featjar.feature.model.analysis.ConstraintProperties;
import de.featjar.formula.structure.IFormula;
import java.util.List;

public class ComputeConstraintProperties extends AComputation<Integer> {
    protected static final Dependency<IFormula> tree = Dependency.newDependency(IFormula.class);

    public ComputeConstraintProperties(IComputation<IFormula> iformula) {
        super(iformula);
    }

    @Override
    public Result<Integer> compute(List<Object> dependencyList, Progress progress) {
        return Trees.traverse(tree.get(dependencyList), new ConstraintProperties());
    }
}
