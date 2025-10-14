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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Enumerates all leafs containing data in order to be processed by CSV formats.
 * Each leaf is represented by a list containing parent name, its name, value, type.
 *
 * @author Mohammad Khair Almekkawi
 * @author Florian Beese
 */
public class AnalysisTreeVisitorCSV implements ITreeVisitor<AnalysisTree<?>, ArrayList<Object>> {
    ArrayList<Object> nodesList = new ArrayList<Object>();

    @Override
    public TraversalAction firstVisit(List<AnalysisTree<?>> path) {
        final AnalysisTree<?> node = ITreeVisitor.getCurrentNode(path);
        if (node.getChildrenCount() == 0 && ITreeVisitor.getParentNode(path).isPresent()) {
            final AnalysisTree<?> parent = ITreeVisitor.getParentNode(path).get();
            nodesList.add(Arrays.asList(
                    parent.getName(), node.getName(), node.getValue().getClass().getName(), node.getValue()));
        }
        return TraversalAction.CONTINUE;
    }

    @Override
    public void reset() {
        nodesList = new ArrayList<Object>();
    }

    @Override
    public Result<ArrayList<Object>> getResult() {

        return Result.of(nodesList);
    }
}
