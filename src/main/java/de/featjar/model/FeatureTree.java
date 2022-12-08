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

import de.featjar.model.util.Mutable;
import de.featjar.util.tree.structure.RootedTree;
import de.featjar.util.tree.structure.Tree;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An ordered {@link RootedTree} labeled with {@link Feature features}.
 * Implements elements of notation from feature-oriented domain analysis, such
 * as mandatory/optional features and groups.
 *
 * @author Elias Kuiter
 */
public class FeatureTree extends RootedTree<FeatureTree> implements Mutable<FeatureTree, FeatureTree.Mutator> {
    /**
     * Feature at the root of this feature tree.
     */
    protected final Feature feature;

    /**
     * Whether this tree's feature is mandatory or optional.
     */
    protected boolean isMandatory = false;

    /**
     * Minimum number of child features that may be selected.
     */
    protected long groupMinimum = 0;

    /**
     * Maximum number of child features that may be selected.
     */
    protected long groupMaximum = Long.MAX_VALUE;

    protected Mutator mutator;

    public FeatureTree(Feature feature) {
        Objects.requireNonNull(feature);
        this.feature = feature;
    }

    public Feature getFeature() {
        return feature;
    }

    public boolean isMandatory() {
        return isMandatory; // todo
    }

    public long getGroupMinimum() {
        return groupMinimum;
    }

    public long getGroupMaximum() {
        return groupMaximum;
    }

    public boolean isAnd() {
        return groupMinimum == 0 && groupMaximum == Long.MAX_VALUE;
    }

    public boolean isAlternative() {
        return groupMinimum == 1 && groupMaximum == 1;
    }

    public boolean isOr() {
        return groupMinimum == 1 && groupMaximum == Long.MAX_VALUE;
    }

    public boolean isGroup() {
        return !isAnd();
    }

    public Set<Constraint> getContainingConstraints() {
        return feature.getFeatureModel().getConstraints().stream()
                .filter(constraint -> constraint.getContainedFeatures().stream().anyMatch(feature::equals))
                .collect(Collectors.toSet());
    }

    @Override
    public Tree<FeatureTree> cloneNode() {
        throw new RuntimeException();
    }

    @Override
    public Mutator getMutator() {
        return mutator == null ? (mutator = new Mutator()) : mutator;
    }

    @Override
    public void setMutator(Mutator mutator) {
        this.mutator = mutator;
    }

    // todo hashcode, equals, tostring, clone

    public class Mutator implements de.featjar.model.util.Mutator<FeatureTree> {
        @Override
        public FeatureTree getMutable() {
            return FeatureTree.this;
        }

        public void setMandatory(boolean mandatory) {
            FeatureTree.this.isMandatory = mandatory;
        }

        public void setGroupMinimum(long groupMinimum) {
            FeatureTree.this.groupMinimum = groupMinimum;
        }

        public void setGroupMaximum(long groupMaximum) {
            FeatureTree.this.groupMaximum = groupMaximum;
        }

        public void setAnd() {
            groupMinimum = 0;
            groupMaximum = Long.MAX_VALUE;
        }

        public void setAlternative() {
            groupMinimum = 1;
            groupMaximum = 1;
        }

        public void setOr() {
            groupMinimum = 1;
            groupMaximum = Long.MAX_VALUE;
        }
    }
}
