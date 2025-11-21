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
package de.featjar.feature.model;

import de.featjar.formula.structure.Expressions;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.predicate.NotEquals;
import de.featjar.formula.structure.term.value.Constant;
import de.featjar.formula.structure.term.value.Variable;

/**
 * Defines useful methods to wrap a bool or numeric feature into a IFormula:
 *      bool: {@link de.featjar.formula.structure.predicate.Literal}
 *      int: {@link NotEquals 0}
 *      float: {@link NotEquals 0}
 *
 * Numeric features are therefore selected, if there value is not 0.
 *
 * @author Jonas Hanke
 */
public class Features {

    public static IFormula createFeatureFormula(IFeature feature) {
        return createFeatureFormula(feature, feature.getName().orElse(""));
    }

    public static IFormula createFeatureFormula(IFeature feature, String featureName) {
        if (feature.getType().equals(Boolean.class)) {
            return Expressions.literal(featureName);
        } else if (feature.getType().equals(Integer.class)) {
            return new NotEquals(new Variable(featureName, feature.getType()), new Constant(0));
        } else if (feature.getType().equals(Float.class)) {
            return new NotEquals(new Variable(featureName, feature.getType()), new Constant(0.0f));
        } else {
            throw new UnsupportedOperationException("Unsupported feature type: " + feature.getType());
        }
    }
}
