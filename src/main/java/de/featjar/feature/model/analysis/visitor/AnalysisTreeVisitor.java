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
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.base.tree.visitor.ITreeVisitor.TraversalAction;
import de.featjar.feature.model.analysis.AnalysisTree;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Transforms a given AnalysisTree into a HashMap 
 * 
 * @author Mohammad Khair Almekkawi
 * @author Florian Beese
 */
public class AnalysisTreeVisitor implements ITreeVisitor<AnalysisTree<?>, HashMap<String, Object>> {
    HashMap<String, Object> nodesMap = new HashMap<String, Object>();

    @Override
    public TraversalAction firstVisit(List<AnalysisTree<?>> path) {
        final AnalysisTree<?> node = ITreeVisitor.getCurrentNode(path);
        HashMap<String, Object> currentMap = nodesMap;

        if (!ITreeVisitor.getParentNode(path).isPresent()) {
            nodesMap.put(ITreeVisitor.getCurrentNode(path).getName(), new HashMap<String, Object>());
            return TraversalAction.CONTINUE;
        }

        for (Iterator<AnalysisTree<?>> iterator = path.iterator(); iterator.hasNext(); ) {
            AnalysisTree<?> currentAnalysisTree = iterator.next();

            if (iterator.hasNext()) {
                currentMap = (HashMap<String, Object>) currentMap.get(currentAnalysisTree.getName());
            }
        }

        if (node.getChildrenCount() == 0) {
            currentMap.put(
                    node.getName(),
                    new ArrayList<Object>(
                            Arrays.asList(node.getName(), node.getValue().getClass(), node.getValue())));
        } else {
            currentMap.put(node.getName(), new HashMap<String, Object>());
        }

        return TraversalAction.CONTINUE;
    }

    @Override
    public void reset() {
        nodesMap = new HashMap<String, Object>();
    }

    @Override
    public Result<HashMap<String, Object>> getResult() {

        return Result.of(nodesMap);
    }
}
