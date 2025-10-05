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
import de.featjar.formula.structure.term.value.Constant;
import de.featjar.formula.structure.term.value.Variable;
import java.util.List;

/**
 * Counts the number of used variables and constants in a tree.
 * By default, both are counted, but it can be set to count only one of the two.
 *
 * @author Mohammad Khair Almekkawi
 * @author Florian Beese
 */
public class AtomsCount implements ITreeVisitor<ITree<?>, Integer> {
    private int atomsCount = 0;
    private boolean countVariables = true;
    private boolean countConstants = true;

    public AtomsCount(boolean countVariables, boolean countConstants) {
        this.countConstants = countConstants;
        this.countVariables = countVariables;
    }

    @Override
    public TraversalAction firstVisit(List<ITree<?>> path) {
        final ITree<?> node = ITreeVisitor.getCurrentNode(path);
        if (countConstants && node instanceof Constant) {
            atomsCount++;
        } else if (countVariables && node instanceof Variable) {
            atomsCount++;
        }
        return TraversalAction.CONTINUE;
    }

    @Override
    public Result<Integer> getResult() {
        return Result.of(atomsCount);
    }

    @Override
    public void reset() {
        atomsCount = 0;
    }
}
