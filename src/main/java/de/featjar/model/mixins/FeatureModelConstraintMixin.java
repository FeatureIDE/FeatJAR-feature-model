/*
 * Copyright (C) 2022 Elias Kuiter
 *
 * This file is part of feature-model.
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
 * See <https://github.com/FeatJAR/model> for further information.
 */
package de.featjar.model.mixins;

import de.featjar.formula.structure.Formula;
import de.featjar.model.Constraint;
import de.featjar.model.FeatureModel;
import de.featjar.model.util.Identifier;
import de.featjar.util.data.Result;
import java.util.*;

/**
 * Implements a {@link FeatureModel} mixin for common operations on
 * {@link Constraint constraints}.
 *
 * @author Elias Kuiter
 */
public interface FeatureModelConstraintMixin {
    List<Constraint> getConstraints(); // todo return also of submodels

    default Optional<Constraint> getConstraint(Identifier identifier) {
        Objects.requireNonNull(identifier);
        return getConstraints().stream()
                .filter(constraint -> constraint.getIdentifier().equals(identifier))
                .findFirst();
    }

    default boolean hasConstraint(Identifier identifier) {
        return getConstraint(identifier).isPresent();
    }

    default boolean hasConstraint(Constraint constraint) {
        return hasConstraint(constraint.getIdentifier());
    }

    default Optional<Integer> getConstraintIndex(Constraint constraint) {
        Objects.requireNonNull(constraint);
        return Result.indexToOptional(getConstraints().indexOf(constraint));
    }

    default int getNumberOfConstraints() {
        return getConstraints().size();
    }

    interface Mutator extends de.featjar.model.util.Mutator<FeatureModel> {
        default void setConstraint(int index, Constraint constraint) {
            Objects.requireNonNull(constraint);
            if (getMutable().hasConstraint(constraint)) {
                throw new IllegalArgumentException();
            }
            getMutable().getConstraints().set(index, constraint);
        }

        default void setConstraints(Iterable<Constraint> constraints) {
            Objects.requireNonNull(constraints);
            getMutable().getConstraints().clear();
            constraints.forEach(this::addConstraint);
        }

        default void addConstraint(Constraint newConstraint, int index) {
            Objects.requireNonNull(newConstraint);
            if (getMutable().hasConstraint(newConstraint)) {
                throw new IllegalArgumentException();
            }
            getMutable().getConstraints().add(index, newConstraint);
        }

        default void addConstraint(Constraint newConstraint) {
            addConstraint(newConstraint, getMutable().getConstraints().size());
        }

        default Constraint createConstraint(Formula formula) {
            Constraint newConstraint = new Constraint(getMutable(), formula);
            addConstraint(newConstraint);
            return newConstraint;
        }

        default Constraint createConstraint(Formula formula, int index) {
            Constraint newConstraint = new Constraint(getMutable(), formula);
            addConstraint(newConstraint, index);
            return newConstraint;
        }

        default Constraint createConstraint() {
            Constraint newConstraint = new Constraint(getMutable());
            addConstraint(newConstraint);
            return newConstraint;
        }

        default void removeConstraint(Constraint constraint) {
            Objects.requireNonNull(constraint);
            if (!getMutable().hasConstraint(constraint)) {
                throw new IllegalArgumentException();
            }
            getMutable().getConstraints().remove(constraint);
        }

        default Constraint removeConstraint(int index) {
            return getMutable().getConstraints().remove(index);
        }
    }

    interface Analyzer extends de.featjar.model.util.Analyzer<FeatureModel> {
        default Set<Constraint> getRedundantConstraints() {
            return Collections.emptySet();
        }

        // ...
    }
}
