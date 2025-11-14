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
import de.featjar.formula.structure.predicate.False;
import de.featjar.formula.structure.predicate.True;
import de.featjar.formula.structure.term.value.Constant;
import de.featjar.formula.structure.term.value.Variable;
import java.util.List;

/**
 * Counts the number of used variables and constants in a tree.
 * By default, both are counted, but it can be set to count only one of the two.
 * For further information on its methods see {@link ITreeVisitor}
 *
 * @author Mohammad Khair Almekkawi
 * @author Florian Beese
 */
public class AtomsCountTreeVisitor implements ITreeVisitor<ITree<?>, Integer> {
    private int atomsCount = 0;
    private boolean countVariables = true;
    private boolean countConstants = true;
    private boolean countBoolean = true;

    /**
     *
     * @param countVariables decide if Atoms of type variable should be counted
     * @param countConstants decide if Atoms of type constants should be counted
     * @param countBoolean decide if Atoms of type True or False should be counted
     */
    public AtomsCountTreeVisitor(boolean countVariables, boolean countConstants, boolean countBoolean) {
        this.countConstants = countConstants;
        this.countVariables = countVariables;
        this.countBoolean = countBoolean;
    }

    @Override
    public TraversalAction firstVisit(List<ITree<?>> path) {
        final ITree<?> node = ITreeVisitor.getCurrentNode(path);
        if (countConstants && node instanceof Constant) {
            atomsCount++;
        } else if (countVariables && node instanceof Variable) {
            atomsCount++;
        } else if (countBoolean && (node instanceof True || node instanceof False)) {
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
