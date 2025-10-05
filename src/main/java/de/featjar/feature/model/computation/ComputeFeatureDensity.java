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
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.base.tree.Trees;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IConstraint;
import de.featjar.feature.model.analysis.FeatureDensity;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ComputeFeatureDensity extends AComputation<Float> {
    protected static final Dependency<FeatureModel> FEATUREMODEL = Dependency.newDependency(FeatureModel.class);

    public ComputeFeatureDensity(IComputation<FeatureModel> featureModel) {
        super(featureModel);
    }

    @Override
    public Result<Float> compute(List<Object> dependencyList, Progress progress) {
        FeatureModel featureModel = FEATUREMODEL.get(dependencyList);
        Collection<IConstraint> Constraints = featureModel.getConstraints();
        Set<String> unionSet = new HashSet<String>();

        Iterator<IConstraint> constraintIterator = Constraints.iterator();
        while (constraintIterator.hasNext()) {
            unionSet.addAll(Trees.traverse(constraintIterator.next().getFormula(), new FeatureDensity())
                    .orElse(Collections.emptySet()));
        }
        return Result.of((float) unionSet.size()
                / (float) FEATUREMODEL.get(dependencyList).getNumberOfFeatures());
    }
}
