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

import de.featjar.formula.structure.atomic.literal.VariableMap;
import de.featjar.model.mixins.*;
import de.featjar.model.util.Analyzable;
import de.featjar.model.util.Identifier;
import de.featjar.model.util.Mutable;
import java.util.*;

/**
 * A feature model is a representation of the space of valid configurations for
 * a software system. Here, we implement feature diagrams (i.e., a
 * {@link FeatureTree} labeled with features and a list of {@link Constraint
 * constraints}). For safe mutation, rely only on the methods of
 * {@link Mutable}.
 *
 * @author Elias Kuiter
 */
public class FeatureModel extends Element
        implements FeatureModelFeatureTreeMixin,
                FeatureModelConstraintMixin,
                FeatureModelFeatureOrderMixin,
                CommonAttributesMixin,
                FeatureModelCacheMixin,
                Mutable<FeatureModel, FeatureModel.Mutator>,
                Analyzable<FeatureModel, FeatureModel.Analyzer> {
    protected final FeatureTree featureTree;
    protected final List<Constraint> constraints = Collections.synchronizedList(new ArrayList<>());
    protected FeatureOrder featureOrder = FeatureOrder.ofPreOrder();

    protected final VariableMap variableMap = new VariableMap(); // todo: get, set, mutate correctly (checks), pass
    // map to createConstraint as Function<...>
    protected final Map<Identifier, Element> elementCache = Collections.synchronizedMap(new LinkedHashMap<>());
    protected final Set<Feature> featureCache = Collections.synchronizedSet(new HashSet<>());
    protected final Set<FeatureModel> featureModelCache = Collections.synchronizedSet(new HashSet<>()); // todo
    // calculate
    // from tree
    protected Mutator mutator;
    protected Analyzer analyzer;
    // todo inv: this featuretree (w/o submodels) has one variablemap. variablemap
    // == features.

    public FeatureModel(Identifier identifier) {
        super(identifier);
        final Feature root = new Feature(this);
        featureTree = root.getFeatureTree();
        finishInternalMutation();
    }

    @Override
    public FeatureTree getFeatureTree() {
        return featureTree;
    }

    // every model returns only its own constraints/features
    @Override
    public List<Constraint> getConstraints() {
        return constraints;
    }

    // TODO? returns constraints of all submodels
    public List<Constraint> getALLConstraints() {
        return constraints;
    }

    @Override
    public FeatureOrder getFeatureOrder() {
        return featureOrder;
    }

    // @Override //constraint mixin?
    public VariableMap getVariableMap() {
        return variableMap;
    }

    @Override
    public Map<Identifier, Element> getElementCache() {
        return elementCache;
    }

    @Override
    public Set<Feature> getFeatureCache() {
        return featureCache;
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
    public Analyzer getAnalyzer() {
        return analyzer == null ? (analyzer = new Analyzer()) : analyzer;
    }

    @Override
    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    @Override
    public void finishInternalMutation() {
        FeatureModelCacheMixin.super.finishInternalMutation();
    }

    @Override
    public String toString() {
        return String.format("FeatureModel{features=%s, constraints=%s}", getFeatures(), constraints);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException(); // todo
    }

    public class Mutator
            implements de.featjar.model.util.Mutator<FeatureModel>,
                    FeatureModelFeatureTreeMixin.Mutator,
                    FeatureModelConstraintMixin.Mutator,
                    FeatureModelFeatureOrderMixin.Mutator,
                    CommonAttributesMixin.Mutator<FeatureModel>,
                    FeatureModelCacheMixin.Mutator {
        @Override
        public FeatureModel getMutable() {
            return FeatureModel.this;
        }

        @Override
        public void setFeatureOrder(FeatureOrder featureOrder) {
            FeatureModel.this.featureOrder = featureOrder;
        }
    }

    public class Analyzer
            implements de.featjar.model.util.Analyzer<FeatureModel>,
                    FeatureModelFeatureTreeMixin.Analyzer,
                    FeatureModelConstraintMixin.Analyzer {
        @Override
        public FeatureModel getAnalyzable() {
            return FeatureModel.this;
        }
    }
}
