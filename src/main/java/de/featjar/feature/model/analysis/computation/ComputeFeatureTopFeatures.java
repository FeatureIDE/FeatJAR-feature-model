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
import de.featjar.feature.model.IFeatureTree;
import java.util.List;

/**
 * Calculates the number of features directly below the root of this subtree.
 *
 * @author Benjamin von Holt
 */
public class ComputeFeatureTopFeatures extends AComputation<Integer> {
    protected static final Dependency<IFeatureTree> FEATURE_TREE = Dependency.newDependency(IFeatureTree.class);

    public ComputeFeatureTopFeatures(IComputation<IFeatureTree> featureTree) {
        super(featureTree);
    }

    @Override
    public Result<Integer> compute(List<Object> dependencyList, Progress progress) {
        IFeatureTree tree = FEATURE_TREE.get(dependencyList);
        return Result.of(tree.getChildrenCount());
    }
}
