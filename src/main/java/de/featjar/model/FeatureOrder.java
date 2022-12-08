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

import de.featjar.model.mixins.FeatureModelFeatureTreeMixin;
import de.featjar.model.util.Mutable;
import de.featjar.util.tree.Trees;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Orders features in different ways. By default, the feature-tree preorder is
 * used.
 *
 * @author Elias Kuiter
 */
public abstract class FeatureOrder
        implements Function<FeatureModelFeatureTreeMixin, List<Feature>>, Mutable<FeatureOrder, FeatureOrder.Mutator> {
    protected boolean isUserDefined;
    protected Mutator mutator;

    public boolean isUserDefined() {
        return isUserDefined;
    }

    @Override
    public FeatureOrder.Mutator getMutator() {
        return mutator == null ? (mutator = new FeatureOrder.Mutator()) : mutator;
    }

    @Override
    public void setMutator(FeatureOrder.Mutator mutator) {
        this.mutator = mutator;
    }

    public static FeatureOrder ofPreOrder() {
        return new FeatureOrder() {
            @Override
            public List<Feature> apply(FeatureModelFeatureTreeMixin featureModel) {
                return Trees.preOrderStream(featureModel.getFeatureTree())
                        .map(FeatureTree::getFeature)
                        .collect(Collectors.toList());
            }
        };
    }

    public static FeatureOrder ofComparator(Comparator<Feature> featureComparator) {
        return new FeatureOrder() {
            @Override
            public List<Feature> apply(FeatureModelFeatureTreeMixin featureModel) {
                return featureModel.getFeatures().stream()
                        .sorted(featureComparator)
                        .collect(Collectors.toList());
            }
        };
    }

    public static FeatureOrder ofList(List<Feature> featureList) { // todo: maybe make this list mutable for easier
        // editing?
        return new FeatureOrder() {
            @Override
            public List<Feature> apply(FeatureModelFeatureTreeMixin featureModel) {
                return Stream.concat(
                                featureList.stream().filter(featureModel.getFeatures()::contains),
                                featureModel.getFeatures().stream().filter(feature -> !featureList.contains(feature)))
                        .collect(Collectors.toList());
            }
        };
    }

    public class Mutator implements de.featjar.model.util.Mutator<FeatureOrder> {
        @Override
        public FeatureOrder getMutable() {
            return FeatureOrder.this;
        }

        public void setUserDefined(boolean userDefined) {
            FeatureOrder.this.isUserDefined = userDefined;
        }
    }
}
