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
import de.featjar.feature.model.io.json.JSONAnalysisFormat;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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

    String name;
    T value;

    public AnalysisTree(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public AnalysisTree(String name, AnalysisTree<?>... children) {
        super(children.length);
        if (children.length > 0) super.setChildren(Arrays.asList(children));
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
        if (other.value == null && this.value != null) {
            return false;
        }
        if (other.value != null && this.value == null) {
            return false;
        }
        if (this.value == null && other.value == null) {
            return this.name.equals(other.name);
        }
        return this.name.equals(other.name) && this.value.equals(other.value);
    }

    @Override
    public int hashCodeNode() {
        return Objects.hash(this.getClass(), this.name, this.value);
    }

    /**
     * Static function to convert a nested HashMap having integers, floats, doubles and other recursively defined HashMaps as value
     * to its AnalysisTree representation.
     *
     * @param hashMap data to convert
     * @param name specifies the name of root
     * @return returns a recursively built tree including its children
     */
    public static AnalysisTree<?> hashMapToTree(HashMap<String, Object> hashMap, String name) {
        AnalysisTree<Object> root = new AnalysisTree<>(name, (Object) null);
        for (Iterator<String> iterator = hashMap.keySet().iterator(); iterator.hasNext(); ) {
            String currentKey = iterator.next();
            if (hashMap.get(currentKey) instanceof Integer) {
                root.addChild(new AnalysisTree<>(currentKey, (int) hashMap.get(currentKey)));
            } else if (hashMap.get(currentKey) instanceof Float) {
                root.addChild(new AnalysisTree<>(currentKey, (float) hashMap.get(currentKey)));
            } else if (hashMap.get(currentKey) instanceof Double) {
                root.addChild(new AnalysisTree<>(currentKey, (double) hashMap.get(currentKey)));
            } else if (hashMap.get(currentKey) instanceof HashMap) {
                root.addChild(hashMapToTree((HashMap<String, Object>) hashMap.get(currentKey), currentKey));
            } else {
                // TODO Add handling for other types or errors if needed
            }
        }
        return root;
    }

    /**
     * Static function to convert a nested hashmap being processed by {@link JSONAnalysisFormat} into its AnalysisTree representation.
     * This function is not supposed to be called initially, otherwise a root node with
     * name having the whole tree as single child is returned.
     *
     * @param hashMap data to convert
     * @param name name of the root
     * @return returns the recursively built tree including its children
     */
    public static AnalysisTree<?> hashMapListToTree(HashMap<String, Object> hashMap, String name) {
        AnalysisTree<Object> root = new AnalysisTree<>(name, (Object) null);
        for (Iterator<String> iterator = hashMap.keySet().iterator(); iterator.hasNext(); ) {
            String currentKey = iterator.next();
            if (hashMap.get(currentKey) instanceof HashMap) {
                root.addChild(hashMapListToTree((HashMap<String, Object>) hashMap.get(currentKey), currentKey));
            } else if (hashMap.get(currentKey) instanceof ArrayList) {
                ArrayList currentElement = (ArrayList) hashMap.get(currentKey);
                if (currentElement.get(1).equals("class java.lang.Double")) {
                    BigDecimal currentDeccimal = (BigDecimal) currentElement.get(2);
                    root.addChild(new AnalysisTree<>(currentKey, currentDeccimal.doubleValue()));
                } else if (currentElement.get(1).equals("class java.lang.Integer")) {
                    root.addChild(new AnalysisTree<>(currentKey, (int) currentElement.get(2)));
                } else if (currentElement.get(1).equals("class java.lang.Float")) {
                    BigDecimal currentDeccimal = (BigDecimal) currentElement.get(2);
                    root.addChild(new AnalysisTree<>(currentKey, currentDeccimal.floatValue()));
                }
            }
        }
        return root;
    }

    /**
     * Static function to convert a nested hashmap being processed by {@link JSONAnalysisFormat} into its AnalysisTree representation.
     * This function is specially suited to process a hashmap having only a single key in its first layer.
     *
     * @param hashMap data to convert
     * @return returns the recursively built tree including its children
     */
    public static AnalysisTree<?> hashMapListToTree(HashMap<String, Object> hashMap) {
        if (hashMap.size() == 1) {
            String key = hashMap.keySet().iterator().next();
            return hashMapListToTree((HashMap<String, Object>) hashMap.get(key), key);
        } else {
            return new AnalysisTree<>("");
        }
    }

    @Override
    public String toString() {
        if (this.value == null) {
            return "Name: " + this.name + " - Value: " + this.value + " - Value class: " + "No class";
        } else {
            return "Name: " + this.name + " - Value: " + this.value + " - Value class: " + this.value.getClass();
        }
    }
}
