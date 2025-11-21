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

import de.featjar.base.data.IAttribute;
import de.featjar.base.data.Name;
import de.featjar.base.data.Result;
import de.featjar.base.data.Void;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.feature.model.constraints.IAttributeAggregate;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implements tree visitor {@link ITreeVisitor}.
 * Each {@link IAttributeAggregate} placeholder in a formula will be replaced with the correct formula.
 *
 * @author Lara Merza
 * @author Felix Behme
 * @author Jonas Hanke
 */
public class ReplaceAttributeAggregate implements ITreeVisitor<IFormula, Void> {

    private final Map<IFormula, Map<IAttribute<?>, Object>> attributes;
    private final boolean hasCardinalityFeatures;

    public ReplaceAttributeAggregate(
            Map<IFormula, Map<IAttribute<?>, Object>> attributes, Boolean hasCardinalityFeatures) {
        this.attributes = attributes;
        this.hasCardinalityFeatures = hasCardinalityFeatures;
    }

    @Override
    public TraversalAction lastVisit(List<IFormula> path) {
        final IExpression expression = ITreeVisitor.getCurrentNode(path);

        if (expression instanceof IAttributeAggregate) {
            if (hasCardinalityFeatures) {
                throw new UnsupportedOperationException(
                        "Attribute aggregates and cardinality features can not be translated.");
            }

            final Result<IFormula> parent = ITreeVisitor.getParentNode(path);
            if (parent.isPresent()) {
                ArrayList<IFormula> filteredFeatures = new ArrayList<>();
                ArrayList<Object> values = new ArrayList<>();
                Name attributeName = ((IAttributeAggregate) expression).getAttributeName();

                // formula -> feature as a formula, value -> attribute map
                attributes.forEach((formula, value) -> {
                    Optional<Map.Entry<IAttribute<?>, Object>> attributeMatch = value.entrySet().stream()
                            .filter(predicate -> predicate.getKey().getName().equals(attributeName))
                            .findFirst();

                    if (attributeMatch.isPresent()) {
                        filteredFeatures.add(formula);
                        values.add(attributeMatch.get().getValue());
                    }
                });

                Result<IExpression> result = ((IAttributeAggregate) expression).translate(filteredFeatures, values);
                if (result.isPresent()) {
                    parent.get().replaceChild(expression, result.get());
                }
            }
        }

        return TraversalAction.CONTINUE;
    }

    @Override
    public Result<Void> getResult() {
        return Result.ofVoid();
    }
}
