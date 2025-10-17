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
import de.featjar.formula.structure.connective.IConnective;
import de.featjar.formula.structure.connective.Reference;
import java.util.HashMap;
import java.util.List;

/**
 * Counts the the absolute occurrence of different operators in a tree.
 * For further information on its methods see {@link ITreeVisitor}
 *
 * @author Mohammad Khair Almekkawi
 * @author Florian Beese
 */
public class OperatorDistributionTreeVisitor implements ITreeVisitor<ITree<?>, HashMap<String, Integer>> {
    // Saves the count of each operator, where each key is the name of the class of the operator
    HashMap<String, Integer> operatorCount = new HashMap<String, Integer>();

    @Override
    public TraversalAction firstVisit(List<ITree<?>> path) {
        final ITree<?> node = ITreeVisitor.getCurrentNode(path);
        if (node instanceof IConnective && !(node instanceof Reference)) {
            String nodeKey = node.getClass().getSimpleName();
            if (!operatorCount.containsKey(nodeKey)) {
                operatorCount.put(nodeKey, 1);
            } else {
                operatorCount.replace(nodeKey, operatorCount.get(nodeKey) + 1);
            }
        }
        return TraversalAction.CONTINUE;
    }

    @Override
    public Result<HashMap<String, Integer>> getResult() {
        return Result.of(operatorCount);
    }

    @Override
    public void reset() {
        operatorCount.clear();
    }
}
