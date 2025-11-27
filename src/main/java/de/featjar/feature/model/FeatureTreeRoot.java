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
package de.featjar.feature.model;

import de.featjar.base.data.IAttribute;
import de.featjar.base.data.Range;
import de.featjar.base.tree.structure.ARootedTree;
import de.featjar.base.tree.structure.ITree;
import de.featjar.feature.model.FeatureTree.Group;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FeatureTreeRoot extends ARootedTree<IFeatureTree> implements IFeatureTree {

    public FeatureTreeRoot() {}

    protected FeatureTreeRoot(FeatureTreeRoot otherFeatureTree) {}

    @Override
    public IFeature getFeature() {
        return null;
    }

    @Override
    public int getParentGroupID() {
        return -1;
    }

    @Override
    public Optional<Group> getParentGroup() {
        return Optional.empty();
    }

    @Override
    public List<Group> getChildrenGroups() {
        return List.of(new Group(Range.atLeast(0)));
    }

    @Override
    public int[] getChildrenGroupIDs() {
        return new int[] {0};
    }

    @Override
    public String toString() {
        return "FeatureTreeRoot";
    }

    @Override
    public Optional<Map<IAttribute<?>, Object>> getAttributes() {
        return Optional.empty();
    }

    @Override
    public List<IFeatureTree> getRoots() {
        return List.of(this);
    }

    @Override
    public int getFeatureCardinalityLowerBound() {
        return 1;
    }

    @Override
    public int getFeatureCardinalityUpperBound() {
        return 1;
    }

    @Override
    public ITree<IFeatureTree> cloneNode() {
        return new FeatureTreeRoot(this);
    }

    @Override
    public boolean equalsNode(IFeatureTree other) {
        return this == other;
    }

    @Override
    public int hashCodeNode() {
        return System.identityHashCode(this);
    }

    public boolean isValidGroupID(int groupID) {
        return groupID == 0;
    }
}
