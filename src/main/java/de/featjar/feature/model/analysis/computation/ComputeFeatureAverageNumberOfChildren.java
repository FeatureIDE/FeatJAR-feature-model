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
import de.featjar.base.tree.Trees;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.analysis.visitor.TreeAvgChildrenCounterTreeVisitor;
import java.util.List;

/**
 * Calculates the Average Number Of Children per Node per Tree
 *
 * @author Benjamin von Holt
 */
public class ComputeFeatureAverageNumberOfChildren extends AComputation<Double> {
    protected static final Dependency<IFeatureTree> FEATURE_TREE = Dependency.newDependency(IFeatureTree.class);

    public ComputeFeatureAverageNumberOfChildren(IComputation<IFeatureTree> featureTree) {
        super(featureTree);
    }

    @Override
    public Result<Double> compute(List<Object> dependencyList, Progress progress) {
        IFeatureTree tree = FEATURE_TREE.get(dependencyList);
        Result<int[]> r = Trees.traverse(tree, new TreeAvgChildrenCounterTreeVisitor());
        if (r.isEmpty()) return Result.of(0.0);
        int[] arr = r.get();
        if (arr.length == 0) return Result.of(0.0);
        long sum = 0;
        for (int c : arr) sum += c;
        return Result.of((double) sum / arr.length);
    }
}
