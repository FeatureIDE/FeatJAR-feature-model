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
import de.featjar.feature.model.analysis.AnalysisTree;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Searches a given {@link AnalysisTree} for nodes containing a {@link #keyword}, and returns these nodes.
 *
 * @author Benjamin von Holt
 */
public class AnalysisTreeKeywordTreeVisitor implements ITreeVisitor<AnalysisTree<?>, HashMap<String, Object>> {

    HashMap<String, Object> foundTrees = new HashMap<>();
    final String keyword;

    /**
     * @param keyword Searches a given {@link AnalysisTree} for nodes containing this keyword.
     */
    public AnalysisTreeKeywordTreeVisitor(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public TraversalAction firstVisit(List<AnalysisTree<?>> path) {
        final AnalysisTree<?> node = ITreeVisitor.getCurrentNode(path);

        if (node.getName().contains(keyword)) {
            if (node.hasChildren()) {
                foundTrees.put(node.getName(), node.getChildren());
            } else if (!node.hasChildren()) {
                foundTrees.put(node.getName(), node.getValue());
            }
        }

        return TraversalAction.CONTINUE;
    }

    @Override
    public void reset() {
        foundTrees = new HashMap<>();
    }

    @Override
    public Result<HashMap<String, Object>> getResult() {
        Map<String, Object> treeMap = new TreeMap<>(foundTrees);
        return Result.of(new LinkedHashMap<>(treeMap));
    }
}
