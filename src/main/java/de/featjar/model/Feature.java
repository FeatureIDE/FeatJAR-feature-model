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

import de.featjar.model.mixins.CommonAttributesMixin;
import de.featjar.model.util.Analyzable;
import de.featjar.model.util.Mutable;
import java.util.Objects;

/**
 * A feature in a {@link FeatureModel} describes some functionality of a
 * software system. It is attached to some feature model and labels a
 * {@link FeatureTree}. For safe mutation, rely only on the methods of
 * {@link Mutable}.
 *
 * @author Elias Kuiter
 */
public class Feature extends Element
        implements CommonAttributesMixin, Mutable<Feature, Feature.Mutator>, Analyzable<Feature, Feature.Analyzer> {
    protected final FeatureModel featureModel;
    protected final FeatureTree featureTree;
    protected Mutator mutator;
    protected Analyzer analyzer;

    public Feature(FeatureModel featureModel) {
        super(featureModel.getNewIdentifier());
        Objects.requireNonNull(featureModel);
        this.featureModel = featureModel;
        featureTree = new FeatureTree(this);
    }

    public FeatureModel getFeatureModel() {
        return featureModel;
    }

    public FeatureTree getFeatureTree() {
        return featureTree;
    }

    public boolean isAbstract() {
        return getAttributeValue(Attributes.ABSTRACT);
    }

    public boolean isConcrete() {
        return !isAbstract();
    }

    public boolean isHidden() {
        return getAttributeValue(Attributes.HIDDEN);
    }

    public boolean isVisible() {
        return !isHidden();
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
    public String toString() {
        return String.format("Feature{name=%s}", getName());
    }

    public class Mutator implements de.featjar.model.util.Mutator<Feature>, CommonAttributesMixin.Mutator<Feature> {
        @Override
        public Feature getMutable() {
            return Feature.this;
        }

        public void setAbstract(boolean value) {
            setAttributeValue(Attributes.ABSTRACT, value);
        }

        public void setHidden(boolean value) {
            setAttributeValue(Attributes.HIDDEN, value);
        }

        public boolean toggleAbstract() {
            return toggleAttributeValue(Attributes.ABSTRACT);
        }

        public boolean toggleHidden() {
            return toggleAttributeValue(Attributes.HIDDEN);
        }

        public void addFeatureBelow(Feature newFeature, int index) {
            getFeatureModel().mutate().addFeatureBelow(newFeature, Feature.this, index);
        }

        public void addFeatureBelow(Feature newFeature) {
            getFeatureModel().mutate().addFeatureBelow(newFeature, Feature.this);
        }

        public void addFeatureNextTo(Feature newFeature) {
            getFeatureModel().mutate().addFeatureNextTo(newFeature, Feature.this);
        }

        public Feature createFeatureBelow(int index) {
            return getFeatureModel().mutate().createFeatureBelow(Feature.this, index);
        }

        public Feature createFeatureBelow() {
            return getFeatureModel().mutate().createFeatureBelow(Feature.this);
        }

        public Feature createFeatureNextTo() {
            return getFeatureModel().mutate().createFeatureNextTo(Feature.this);
        }

        public void remove() {
            getFeatureModel().mutate().removeFeature(Feature.this);
        }
    }

    public class Analyzer implements de.featjar.model.util.Analyzer<Feature> {
        @Override
        public Feature getAnalyzable() {
            return Feature.this;
        }

        public boolean isDead() {
            return false;
        }

        // ...
    }
}
