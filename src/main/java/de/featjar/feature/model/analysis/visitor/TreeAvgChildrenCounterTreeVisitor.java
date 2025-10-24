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
package de.featjar.feature.model.analysis.visitor;

import de.featjar.base.data.Result;
import de.featjar.base.tree.structure.ITree;
import de.featjar.base.tree.visitor.ITreeVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates the average amount of children per node in the tree.
 * Returns 0 if tree has no nodes.
 *
 * @author Valentin Laubsch
 * @author Benjamin von Holt
 */
public class TreeAvgChildrenCounterTreeVisitor implements ITreeVisitor<ITree<?>, int[]> {
    private ArrayList<Integer> childrenPerNode = new ArrayList<>();

    @Override
    public TraversalAction firstVisit(List<ITree<?>> path) {
        final ITree<?> node = ITreeVisitor.getCurrentNode(path);
        childrenPerNode.add(node.getChildrenCount());
        return TraversalAction.CONTINUE;
    }

    @Override
    public void reset() {
        childrenPerNode.clear();
    }

    /**
     * The old Visitor calculated the average number, instead of returning the Array
     * In case the old funktionality is needed it is implemented as getAverage() now.
     *
     * @return Average Number Of Children per Node as double for whole tree
     * @author Valentin Laubsch
     */
    public Result<Double> getAverage() {
        if (childrenPerNode.isEmpty()) return Result.of(0.0);
        long sum = 0;
        for (int c : childrenPerNode) sum += c;
        double avg = (double) sum / childrenPerNode.size();
        return Result.of(avg);
    }

    @Override
    public Result<int[]> getResult() {
        int[] resultArray = new int[childrenPerNode.size()];
        for (int i = 0; i < childrenPerNode.size(); i++) {
            resultArray[i] = childrenPerNode.get(i);
        }
        return Result.of(resultArray);
    }
}
