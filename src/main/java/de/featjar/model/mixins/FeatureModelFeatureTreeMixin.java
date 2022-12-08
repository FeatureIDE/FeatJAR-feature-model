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

import de.featjar.model.Feature;
import de.featjar.model.FeatureModel;
import de.featjar.model.FeatureTree;
import de.featjar.model.util.Identifier;
import de.featjar.util.tree.Trees;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implements a {@link FeatureModel} mixin for common operations on the
 * {@link FeatureTree}.
 *
 * @author Elias Kuiter
 */
public interface FeatureModelFeatureTreeMixin {
    FeatureTree getFeatureTree();

    default Set<Feature> getFeatures() {
        return Trees.preOrderStream(getFeatureTree())
                .map(FeatureTree::getFeature)
                .collect(Collectors.toSet());
    }

    default int getNumberOfFeatures() {
        return getFeatures().size();
    }

    default Feature getRootFeature() {
        return getFeatureTree().getRoot().getFeature();
    }

    default Optional<Feature> getFeature(Identifier identifier) {
        Objects.requireNonNull(identifier);
        return getFeatures().stream()
                .filter(feature -> feature.getIdentifier().equals(identifier))
                .findFirst();
    }

    default Set<Feature> getFeaturesByName(String name) {
        Objects.requireNonNull(name);
        return getFeatures().stream()
                .filter(feature -> feature.getName().equals(name))
                .collect(Collectors.toSet());
    }

    default boolean hasFeature(Identifier identifier) {
        return getFeature(identifier).isPresent();
    }

    default boolean hasFeature(Feature feature) {
        return hasFeature(feature.getIdentifier());
    }

    interface Mutator extends de.featjar.model.util.Mutator<FeatureModel> {
        default void addFeatureBelow(Feature newFeature, Feature parentFeature, int index) {
            Objects.requireNonNull(newFeature);
            Objects.requireNonNull(parentFeature);
            if (getMutable().hasFeature(newFeature) || !getMutable().hasFeature(parentFeature)) {
                throw new IllegalArgumentException();
            }
            parentFeature.getFeatureTree().addChild(index, newFeature.getFeatureTree());
        }

        default void addFeatureBelow(Feature newFeature, Feature parentFeature) {
            Objects.requireNonNull(newFeature);
            Objects.requireNonNull(parentFeature);
            addFeatureBelow(
                    newFeature, parentFeature, parentFeature.getFeatureTree().getNumberOfChildren());
        }

        default void addFeatureNextTo(Feature newFeature, Feature siblingFeature) {
            Objects.requireNonNull(newFeature);
            Objects.requireNonNull(siblingFeature);
            if (!siblingFeature.getFeatureTree().hasParent() || !getMutable().hasFeature(siblingFeature)) {
                throw new IllegalArgumentException();
            }
            addFeatureBelow(
                    newFeature,
                    siblingFeature.getFeatureTree().getParent().get().getFeature(),
                    siblingFeature.getFeatureTree().getIndex().get() + 1);
        }

        default Feature createFeatureBelow(Feature parentFeature, int index) {
            Feature newFeature = new Feature(getMutable());
            addFeatureBelow(newFeature, parentFeature, index);
            return newFeature;
        }

        default Feature createFeatureBelow(Feature parentFeature) {
            Feature newFeature = new Feature(getMutable());
            addFeatureBelow(newFeature, parentFeature);
            return newFeature;
        }

        default Feature createFeatureNextTo(Feature siblingFeature) {
            Feature newFeature = new Feature(getMutable());
            addFeatureNextTo(newFeature, siblingFeature);
            return newFeature;
        }

        default void removeFeature(Feature feature) { // todo what about the containing constraints?
            Objects.requireNonNull(feature);
            if (feature.equals(getMutable().getRootFeature()) || !getMutable().hasFeature(feature)) {
                throw new IllegalArgumentException();
            }

            final FeatureTree parentFeatureTree =
                    feature.getFeatureTree().getParent().get();

            if (parentFeatureTree.getNumberOfChildren() == 1) {
                parentFeatureTree.mutate(mutator -> {
                    if (feature.getFeatureTree().isAnd()) {
                        mutator.setAnd();
                    } else if (feature.getFeatureTree().isAlternative()) {
                        mutator.setAlternative();
                    } else {
                        mutator.setOr();
                    }
                });
            }

            final int index = feature.getFeatureTree().getIndex().get();
            while (feature.getFeatureTree().hasChildren()) {
                parentFeatureTree.addChild(
                        index,
                        feature.getFeatureTree()
                                .removeChild(feature.getFeatureTree().getNumberOfChildren() - 1));
            }

            parentFeatureTree.removeChild(feature.getFeatureTree());
        }
    }

    interface Analyzer extends de.featjar.model.util.Analyzer<FeatureModel> {
        default Set<Feature> getCoreFeatures() {
            return Collections.emptySet();
        }

        default Set<Feature> getDeadFeatures() {
            return Collections.emptySet(); // use extensions
        }

        // ...
    }
}
