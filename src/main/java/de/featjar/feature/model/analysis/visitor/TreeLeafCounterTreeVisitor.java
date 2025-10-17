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
import java.util.List;

/**
 * Counts the number of nodes that have no child nodes
 * Can be passed a class up to which should be counted (e.g., to exclude details in a tree).
 *
 * @author Valentin Laubsch
 * @author Benjamin von Holt
 */
public class TreeLeafCounterTreeVisitor implements ITreeVisitor<ITree<?>, Integer> {
    private int leafCount = 0;

    @Override
    public TraversalAction firstVisit(List<ITree<?>> path) {
        final ITree<?> node = ITreeVisitor.getCurrentNode(path);
        if (!node.hasChildren()) {
            leafCount++;
        }
        return TraversalAction.CONTINUE;
    }

    @Override
    public void reset() {
        leafCount = 0;
    }

    @Override
    public Result<Integer> getResult() {
        return Result.of(leafCount);
    }
}
