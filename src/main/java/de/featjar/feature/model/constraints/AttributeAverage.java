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
package de.featjar.feature.model.constraints;

import de.featjar.base.data.Name;
import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.tree.structure.ITree;
import de.featjar.formula.structure.ATerminalExpression;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.term.ITerm;
import de.featjar.formula.structure.term.IfThenElse;
import de.featjar.formula.structure.term.function.AAdd;
import de.featjar.formula.structure.term.function.integer.IntegerAdd;
import de.featjar.formula.structure.term.function.real.RealAdd;
import de.featjar.formula.structure.term.function.real.RealDivide;
import de.featjar.formula.structure.term.value.Constant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * The average aggregate placeholder sums attribute values from attributes with a specific attribute name and divides
 * the sum by the number of attributes.
 * Only boolean features which are selected ({@link de.featjar.formula.structure.term.value.Variable} with
 * type {@link Boolean} and value true) will be considered.
 *
 * @author Lara Merza
 * @author Felix Behme
 * @author Jonas Hanke
 */
public class AttributeAverage extends ATerminalExpression implements IAttributeAggregate {

    private final Name attributeName;

    public AttributeAverage(Name attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public String getName() {
        return "avg(" + attributeName + ")";
    }

    @Override
    public Class<?> getType() {
        return Double.class;
    }

    @Override
    public Optional<?> evaluate(List<?> values) {
        return Optional.empty();
    }

    @Override
    public ITree<IExpression> cloneNode() {
        return new AttributeAverage(attributeName);
    }

    @Override
    public Name getAttributeName() {
        return attributeName;
    }

    @Override
    public Result<IExpression> translate(List<IFormula> formulas, List<?> values) {
        if (formulas == null || formulas.isEmpty()) {
            return Result.empty(new Problem("Formulas is null or empty"));
        }
        if (values == null || values.isEmpty()) {
            return Result.empty(new Problem("Values is null or empty"));
        }
        if (formulas.size() != values.size()) {
            return Result.empty(new Problem("Size of formulas is unequal to size of values"));
        }
        for (Object value : values) {
            if (value == null) {
                return Result.empty(new Problem("Values must not be null"));
            }
        }

        Object firstValue = values.get(0);
        Constant zero;
        Class<?> type;
        AAdd addNode;
        if (firstValue instanceof Double) {
            zero = new Constant(0.0, Double.class);
            type = Double.class;
            addNode = new RealAdd();
        } else if (firstValue instanceof Long) {
            zero = new Constant(0L, Long.class);
            type = Long.class;
            addNode = new IntegerAdd();
        } else {
            return Result.empty(
                    new Problem(String.format("Unsupported type <%s> for attribute sum", firstValue.getClass())));
        }

        for (Object value : values) {
            if (type != value.getClass()) {
                return Result.empty(
                        new Problem(String.format("Attributes of differing types %s and %s", value.getClass(), type)));
            }
        }

        List<ITerm> termListSum = new ArrayList<>();
        List<ITerm> termListCount = new ArrayList<>();
        Iterator<IFormula> iterator = formulas.iterator();
        for (Object value : values) {
            IFormula condition = iterator.next();
            termListSum.add(new IfThenElse(condition, new Constant(type.cast(value), type), zero));
            termListCount.add(new IfThenElse(condition, new Constant(1L), new Constant(0L)));
        }

        addNode.setChildren(termListSum);
        return Result.of(new RealDivide(addNode, new IntegerAdd(termListCount)));
    }
}
