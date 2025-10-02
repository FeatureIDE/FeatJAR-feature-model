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
package de.featjar.feature.model.analysis;

import de.featjar.base.data.Result;
import de.featjar.base.tree.structure.ITree;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.formula.structure.connective.IConnective;
import java.util.HashMap;
import java.util.List;

public class OperatorDistribution implements ITreeVisitor<ITree<?>, HashMap<String, Integer>> {
    HashMap<String, Integer> operatorCountMap = new HashMap<String, Integer>();

    @Override
    public TraversalAction firstVisit(List<ITree<?>> path) {
        final ITree<?> node = ITreeVisitor.getCurrentNode(path);
        if (node instanceof IConnective) {
            String nodeKey = node.getClass().getSimpleName();
            if (!operatorCountMap.containsKey(nodeKey)) {
                operatorCountMap.put(nodeKey, 1);
            } else {
                operatorCountMap.replace(nodeKey, operatorCountMap.get(nodeKey) + 1);
            }
        }
        return TraversalAction.CONTINUE;
    }

    @Override
    public Result<HashMap<String, Integer>> getResult() {
        return Result.of(operatorCountMap);
    }

    @Override
    public void reset() {
        operatorCountMap.clear();
    }
}
