/*
 * Copyright (C) 2025 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-base.
 *
 * base is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * base is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with base. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-base> for further information.
 */

package de.featjar.feature.model.analysis.visitor;

import de.featjar.base.data.Result;
import de.featjar.base.tree.structure.ITree;
import de.featjar.base.tree.visitor.ITreeVisitor;

import java.util.List;

/**
 * Calculates the average amount of children per node in the tree.
 * Returns 0 if tree has no nodes.
 *
 * @author Sebastian Krieter
 */
public class TreeAvgChildrenCounter implements ITreeVisitor<ITree<?>, Integer> {
    private Class<? extends ITree<?>> terminalClass = null;
    private int nodeCount = 0;
    private int childCount = 0;

    public Class<? extends ITree<?>> getTerminalClass() {
        return terminalClass;
    }

    public void setTerminalClass(Class<? extends ITree<?>> terminalClass) {
        this.terminalClass = terminalClass;
    }

    @Override
    public TraversalAction firstVisit(List<ITree<?>> path) {
        final ITree<?> node = ITreeVisitor.getCurrentNode(path);

        nodeCount++;
        childCount += node.getChildrenCount();

        // if only nodes with children should be counted
        /*
        if (node.hasChildren()) {
            nodeCount++;
            childCount += node.getChildrenCount();
        }
        */

        return TraversalAction.CONTINUE;
    }

    @Override
    public void reset() {
        nodeCount = 0;
        childCount = 0;
    }

    @Override
    public Result<Integer> getResult() {
        // currently uses int because float and double are illegal inputs for Result.of()
        int result = 0;
        if (nodeCount > 0) {
            result = childCount /nodeCount;
        }
        return Result.of(result);
    }
}
