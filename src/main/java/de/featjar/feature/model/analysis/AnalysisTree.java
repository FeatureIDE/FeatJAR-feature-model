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
package de.featjar.feature.model.analysis;

import de.featjar.base.tree.structure.ATree;
import de.featjar.feature.model.analysis.util.ValueUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A tree of nodes with a given name and some data.
 *
 * @param <T> type of value this node holds
 * @author Mohammad Khair Almekkawi
 * @author Florian Beese
 */
public class AnalysisTree<T> extends ATree<AnalysisTree<?>> {

    private String name;
    private T value;

    public AnalysisTree(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public AnalysisTree(String name, AnalysisTree<?> firstchild, AnalysisTree<?>... children) {
        super(children.length + 1);
        ArrayList<AnalysisTree<?>> allChildren = new ArrayList<>();
        allChildren.add(firstchild);
        java.util.Collections.addAll(allChildren, children);
        if (allChildren.size() > 0) super.setChildren(allChildren);
        this.name = name;
    }

    public AnalysisTree(String name, List<? extends AnalysisTree<?>> children) {
        super(children.size());
        super.setChildren(children);
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public T getValue() {
        return this.value;
    }

    public AnalysisTree(AnalysisTree<T> analysisTree) {
        this.name = analysisTree.name;
        this.value = analysisTree.value;
    }

    @Override
    public AnalysisTree<?> cloneNode() {
        return new AnalysisTree<>(this);
    }

    @Override
    public boolean equalsNode(AnalysisTree<?> other) {
        return Objects.equals(name, other.name) && ValueUtils.equalsValue(value, other.value);
    }

    @Override
    public int hashCodeNode() {
        return Objects.hash(getClass(), name, ValueUtils.hashValue(value));
    }

    @Override
    public String toString() {
        return "Name: " + name + " - Value: " + ValueUtils.toStringValue(value);
    }
}
