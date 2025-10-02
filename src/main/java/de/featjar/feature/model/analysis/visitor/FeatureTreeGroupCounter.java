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
import de.featjar.feature.model.FeatureTree;
import de.featjar.feature.model.IFeatureTree;

import java.util.HashMap;
import java.util.List;

/**
 * Counts the share of groups found in the given feature tree, in order: Or, And, Alternate.
 *
 * @author Sebastian Krieter
 */
public class FeatureTreeGroupCounter implements ITreeVisitor<ITree<IFeatureTree>, HashMap<String, Integer>> {
    private Class<? extends ITree<IFeatureTree>> terminalClass = null;
    int altCounter = 0, orCounter = 0, andCounter = 0;

    public Class<? extends ITree<IFeatureTree>> getTerminalClass() {
        return terminalClass;
    }

    public void setTerminalClass(Class<? extends ITree<IFeatureTree>> terminalClass) {
        this.terminalClass = terminalClass;
    }

    @Override
    public TraversalAction firstVisit(List<ITree<IFeatureTree>> path) {
        final IFeatureTree tree = (IFeatureTree) ITreeVisitor.getCurrentNode(path);

        for (FeatureTree.Group group : tree.getChildrenGroups()) {
            if (group.isAlternative()) {
                altCounter++;
            } else if (group.isOr()) {
                orCounter++;
            } else if (group.isAnd()) {
                andCounter++;
            }
        }

        return TraversalAction.CONTINUE;
    }

    @Override
    public void reset() {
        altCounter = 0;
        orCounter = 0;
        andCounter = 0;
    }

    @Override
    public Result<HashMap<String, Integer>> getResult() {
        HashMap<String, Integer> countedGroups = new HashMap<>();
        countedGroups.put("AlternativeGroup", altCounter);
        countedGroups.put("OrGroup", orCounter);
        countedGroups.put("AndGroup", andCounter);
        return Result.of(countedGroups);
    }
}
