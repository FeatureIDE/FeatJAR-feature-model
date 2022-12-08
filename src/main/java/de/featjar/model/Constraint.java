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
package de.featjar.model;

import de.featjar.formula.structure.Formula;
import de.featjar.formula.structure.Formulas;
import de.featjar.formula.structure.atomic.literal.Literal;
import de.featjar.model.mixins.CommonAttributesMixin;
import de.featjar.model.util.Analyzable;
import de.featjar.model.util.Identifier;
import de.featjar.model.util.Mutable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A constraint describes some restriction of the valid configurations
 * represented by a {@link FeatureModel}. It is attached to some feature model
 * and represented as a {@link Formula} over {@link Feature features}. For safe
 * mutation, rely only on the methods of {@link Mutable}.
 *
 * @author Elias Kuiter
 */
public class Constraint extends Element
        implements Mutable<Constraint, Constraint.Mutator>, Analyzable<Constraint, Constraint.Analyzer> {
    protected final FeatureModel featureModel;
    protected Formula formula;
    protected final Set<Feature> containedFeaturesCache = new HashSet<>();
    protected Mutator mutator;
    protected Analyzer analyzer;

    public Constraint(FeatureModel featureModel, Formula formula) {
        super(featureModel.getNewIdentifier());
        Objects.requireNonNull(featureModel);
        this.featureModel = featureModel;
        getMutator().setFormula(formula); // todo efficient?
    }

    public Constraint(FeatureModel featureModel) {
        super(featureModel.getNewIdentifier());
        Objects.requireNonNull(featureModel);
        this.featureModel = featureModel;
        this.formula = Literal.True;
    }

    public FeatureModel getFeatureModel() {
        return featureModel;
    }

    public Formula getFormula() {
        return formula;
    }

    public Set<Feature> getContainedFeatures() {
        return containedFeaturesCache;
    }

    public Set<String> getTags() {
        return getAttributeValue(Attributes.TAGS);
    }

    @Override
    public Mutator getMutator() {
        return mutator == null ? (mutator = new Mutator()) : mutator;
    }

    @Override
    public void setMutator(Mutator mutator) {
        this.mutator = mutator;
    }

    @Override
    public String toString() {
        return String.format("Constraint{formula=%s}", formula);
    }

    @Override
    public Analyzer getAnalyzer() {
        return analyzer == null ? (analyzer = new Analyzer()) : analyzer;
    }

    @Override
    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public class Mutator
            implements de.featjar.model.util.Mutator<Constraint>, CommonAttributesMixin.Mutator<Constraint> {
        @Override
        public Constraint getMutable() {
            return Constraint.this;
        }

        public void setFormula(Formula formula) {
            Objects.requireNonNull(formula);
            Set<Identifier> identifiers = Formulas.getVariableNames(formula).stream()
                    .map(getIdentifier().getFactory()::parse)
                    .collect(Collectors.toSet());
            Optional<Identifier> unknownIdentifier = identifiers.stream()
                    .filter(identifier -> !featureModel.hasFeature(identifier))
                    .findAny();
            if (unknownIdentifier.isPresent()) {
                throw new RuntimeException("encountered unknown identifier " + unknownIdentifier.get());
            }
            containedFeaturesCache.clear();
            containedFeaturesCache.addAll(identifiers.stream()
                    .map(featureModel::getFeature)
                    .map(Optional::get)
                    .collect(Collectors.toSet()));
            Constraint.this.formula = formula;
        }

        public void setTags(Set<String> tags) {
            setAttributeValue(Attributes.TAGS, tags);
        }

        public void remove() {
            getFeatureModel().mutate().removeConstraint(Constraint.this);
        }
    }

    public class Analyzer implements de.featjar.model.util.Analyzer<Constraint> {
        @Override
        public Constraint getAnalyzable() {
            return Constraint.this;
        }

        public boolean isRedundant() {
            return false;
        }

        // ...
    }
}
