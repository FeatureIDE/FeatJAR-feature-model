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
package de.featjar.feature.model.transformer;

import de.featjar.feature.model.IFeature;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.predicate.NotEquals;
import de.featjar.formula.structure.term.value.Constant;
import de.featjar.formula.structure.term.value.Variable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Defines useful methods to wrap a bool or numeric feature into a IFormula:
 *      bool: {@link de.featjar.formula.structure.predicate.Literal}
 *      int: {@link NotEquals 0}
 *      float: {@link NotEquals 0}
 *
 * Numeric features are therefore selected, if there value is not 0.
 *
 * @author Jonas Hanke
 * @author Sebastian Krieter
 */
public class FeatureToFormula {

    private final Map<String, IFormula> nameToFormula = new LinkedHashMap<>();
    private final Map<String, LinkedHashSet<String>> featureNameToNames = new LinkedHashMap<>();
    private final LinkedHashSet<Variable> variables = new LinkedHashSet<>();

    public IFormula getFeatureFormula2(String featureName) {
        return nameToFormula.get(featureName);
    }

    public Collection<String> getNamesPerFeature(String featureName) {
        return Collections.unmodifiableCollection(featureNameToNames.get(featureName));
    }

    public IFormula getOrCreateFeatureFormula(IFeature feature) {
        return getOrCreateFeatureFormula(feature, feature.getName().orElse("???"));
    }

    public IFormula getOrCreateFeatureFormula(IFeature feature, String featureName) {
        IFormula formula = nameToFormula.get(featureName);
        if (formula == null) {
            formula = newFeatureFormula(feature, featureName);
            nameToFormula.put(featureName, formula);
            featureNameToNames
                    .computeIfAbsent(feature.getName().orElse("???"), k -> new LinkedHashSet<>())
                    .add(featureName);
        }
        return formula;
    }

    private IFormula newFeatureFormula(IFeature feature, String featureName) {
        Class<?> type = feature.getType();
        Variable variable = new Variable(featureName, type);
        variables.add(variable);

        if (type.equals(Boolean.class)) {
            return new Literal(variable);
        } else if (type.equals(Integer.class)) {
            return new NotEquals(variable, new Constant(0));
        } else if (type.equals(Double.class)) {
            return new NotEquals(variable, new Constant(0.0));
        } else if (type.equals(Long.class)) {
            return new NotEquals(variable, new Constant(0L));
        } else if (type.equals(Float.class)) {
            return new NotEquals(variable, new Constant(0.0f));
        } else {
            return new NotEquals(variable, new Constant(null, type));
        }
    }

    public Collection<Variable> getVariables() {
        return Collections.unmodifiableCollection(variables);
    }
}
