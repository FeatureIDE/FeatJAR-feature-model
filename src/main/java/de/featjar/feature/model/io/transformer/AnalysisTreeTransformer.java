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
package de.featjar.feature.model.io.transformer;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.feature.model.analysis.AnalysisTree;
import de.featjar.feature.model.io.json.JSONAnalysisFormat;
import de.featjar.feature.model.io.yaml.YAMLAnalysisFormat;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import static de.featjar.feature.model.analysis.util.AnalysisArrays.isSeries;
import static de.featjar.feature.model.analysis.util.AnalysisArrays.toDoubleArray;

/**
 * A class that handles transformations into AnalysisTree.
 *
 * @author Mohammad Khair Almekkawi
 * @author Florian Beese
 */
public class AnalysisTreeTransformer {

    /**
     * Static function to convert a nested HashMap having integers, floats, doubles and other recursively defined HashMaps as value
     * to its AnalysisTree representation.
     *
     * @param hashMap data to convert
     * @param name specifies the name of root
     * @return on success returns a recursively built tree including its children wrapped in a Result<>.
     * On failure return an empty Result
     */
    public static Result<AnalysisTree<?>> hashMapToTree(HashMap<String, Object> hashMap, String name) {
        AnalysisTree<Object> root = new AnalysisTree<>(name, (Object) null);
        for (Iterator<Entry<String, Object>> iterator = hashMap.entrySet().iterator(); iterator.hasNext(); ) {
            Entry<String, Object> currentEntry = iterator.next();
            String currentKey = currentEntry.getKey();
            Object currentValue = currentEntry.getValue();
            if (currentValue instanceof Integer) {
                root.addChild(new AnalysisTree<>(currentKey, (int) currentValue));
            } else if (currentValue instanceof Float) {
                root.addChild(new AnalysisTree<>(currentKey, (float) currentValue));
            } else if (currentValue instanceof Double) {
                root.addChild(new AnalysisTree<>(currentKey, (double) currentValue));
            } else if (currentValue instanceof double[]) {
                root.addChild(new AnalysisTree<>(currentKey, (double[]) currentValue));
            } else if (currentValue instanceof HashMap) {
                Result<AnalysisTree<?>> result = hashMapToTree((HashMap<String, Object>) currentValue, currentKey);
                if (result.isPresent()) {
                    root.addChild(result.get());
                } else {
                    return Result.empty();
                }

            } else {
                FeatJAR.log()
                        .error("An innermost element of the Map data structure was not of type "
                                + "Float, Double, Integer, double[], or HashMap");
                return Result.empty();
            }
        }
        return Result.of(root);
    }

    /**
     * Static function to convert a nested HashMap being processed by {@link JSONAnalysisFormat} into its AnalysisTree representation.
     * This function is not supposed to be called initially, otherwise a root node with
     * name having the whole tree as single child is returned.
     *
     * @param hashMap data to convert
     * @param name name of the root
     * @return on success returns a recursively built tree including its children wrapped in a Result<>.
     * On failure return an empty Result
     */
    public static Result<AnalysisTree<?>> jsonHashMapToTree(HashMap<String, Object> hashMap, String name) {
        AnalysisTree<Object> root = new AnalysisTree<>(name, (Object) null);
        for (Iterator<Entry<String, Object>> iterator = hashMap.entrySet().iterator(); iterator.hasNext(); ) {
            Entry<String, Object> currentEntry = iterator.next();
            String currentKey = currentEntry.getKey();
            Object currentValue = currentEntry.getValue();
            if (currentValue instanceof HashMap) {
                Result<AnalysisTree<?>> result = jsonHashMapToTree((HashMap<String, Object>) currentValue, currentKey);
                if (result.isPresent()) {
                    root.addChild(result.get());
                } else {
                    return result;
                }
            } else if (currentValue instanceof ArrayList) {
                ArrayList<?> currentElement = (ArrayList<?>) currentValue;
                if (!(currentElement.size() == 3)) {
                    FeatJAR.log()
                            .error("An innermost element of the Map/YAML data structure does not contain "
                                    + "exactly three elements");
                    return Result.empty();
                }
                if (!(currentElement.get(0) instanceof String)) {
                    FeatJAR.log()
                            .error("The first element of an innermost element of the Map/YAML data structure "
                                    + "was not from the type String");
                    return Result.empty();
                }
                if (!(currentElement.get(1) instanceof String)) {
                    FeatJAR.log()
                            .error("The second element of an innermost element of the Map/YAML data structure "
                                    + "was not from the type String");
                    return Result.empty();
                }
                if (!(currentElement.get(2) instanceof BigDecimal || currentElement.get(2) instanceof Integer)) {
                    FeatJAR.log()
                            .error("The third element of an innermost element of the Map/YAML data structure "
                                    + "was not from the type String");
                    return Result.empty();
                }

                if (currentElement.get(1).equals("java.lang.Double")) {
                    if (currentElement.get(2) instanceof BigDecimal) {
                        BigDecimal currentDeccimal = (BigDecimal) currentElement.get(2);
                        root.addChild(new AnalysisTree<>(currentKey, currentDeccimal.doubleValue()));
                    } else if (currentElement.get(2) instanceof Integer) {
                        Integer intValue = (Integer) currentElement.get(2);
                        root.addChild(new AnalysisTree<>(currentKey, intValue.doubleValue()));
                    } else {
                        return Result.empty();
                    }
                } else if (currentElement.get(1).equals("java.lang.Integer")) {
                    root.addChild(new AnalysisTree<>(currentKey, (int) currentElement.get(2)));
                } else if (currentElement.get(1).equals("java.lang.Float")) {
                    if (currentElement.get(2) instanceof BigDecimal) {
                        BigDecimal currentDeccimal = (BigDecimal) currentElement.get(2);
                        root.addChild(new AnalysisTree<>(currentKey, currentDeccimal.floatValue()));
                    } else if (currentElement.get(2) instanceof Integer) {
                        Integer intValue = (Integer) currentElement.get(2);
                        root.addChild(new AnalysisTree<>(currentKey, intValue.floatValue()));
                    } else {
                        return Result.empty();
                    }
                }
            }
        }
        return Result.of(root);
    }

    /**
     * Static function to convert a nested HashMap being processed by {@link JSONAnalysisFormat} into its AnalysisTree representation.
     * This function is specially suited to process a HashMap having only a single key in its first layer.
     *
     * @param hashMap data to convert
     * @return on success returns a recursively built tree including its children wrapped in a Result<>.
     * On failure return an empty Result
     */
    public static Result<AnalysisTree<?>> jsonHashMapToTree(HashMap<String, Object> hashMap) {
        if (hashMap.size() == 1) {
            Entry<String, Object> currentEntry = hashMap.entrySet().iterator().next();
            return jsonHashMapToTree((HashMap<String, Object>) currentEntry.getValue(), currentEntry.getKey());
        } else {
            return Result.empty();
        }
    }

    /**
     * Static function to convert a nested HashMap being processed by {@link YAMLAnalysisFormat} into its AnalysisTree representation.
     * This function is not supposed to be called initially, otherwise a root node with
     * name having the whole tree as single child is returned.
     *
     * @param hashMap data to convert
     * @return on success returns a recursively built tree including its children wrapped in a Result<>.
     * On failure return an empty Result
     */
    public static Result<AnalysisTree<?>> yamlHashMapToTree(HashMap<String, Object> hashMap, String name) {
        AnalysisTree<Object> root = new AnalysisTree<>(name, (Object) null);
        for (Iterator<Entry<String, Object>> iterator = hashMap.entrySet().iterator(); iterator.hasNext(); ) {
            Entry<String, Object> currentEntry = iterator.next();
            String currentKey = currentEntry.getKey();
            Object currentValue = currentEntry.getValue();
            if (currentValue instanceof HashMap) {
                Result<AnalysisTree<?>> result = yamlHashMapToTree((HashMap<String, Object>) currentValue, currentKey);
                if (result.isPresent()) {
                    root.addChild(result.get());
                } else {
                    return result;
                }
            } else if (currentValue instanceof ArrayList) {
                ArrayList<?> currentElement = (ArrayList<?>) currentValue;
                if (!(currentElement.size() == 3)) {
                    FeatJAR.log()
                            .error("An innermost element of the Map/YAML data structure does not contain "
                                    + "exactly three elements");
                    return Result.empty();
                }
                if (!(currentElement.get(0) instanceof String)) {
                    FeatJAR.log()
                            .error("The first element of an innermost element of the Map/YAML data structure "
                                    + "was not from the type String");
                    return Result.empty();
                }
                if (!(currentElement.get(1) instanceof String)) {
                    FeatJAR.log()
                            .error("The second element of an innermost element of the Map/YAML data structure "
                                    + "was not from the type String");
                    return Result.empty();
                }
                if (!(currentElement.get(2) instanceof Double || currentElement.get(2) instanceof Integer)) {
                    FeatJAR.log()
                            .error("The third element of an innermost element of the Map/YAML data structure "
                                    + "was not from the type String");
                    return Result.empty();
                }
                Object typeString = currentElement.get(1);
                if (typeString.equals("java.lang.Double")) {
                    double currentDeccimal = (double) currentElement.get(2);
                    root.addChild(new AnalysisTree<>(currentKey, currentDeccimal));
                } else if (typeString.equals("java.lang.Integer")) {
                    root.addChild(new AnalysisTree<>(currentKey, (int) currentElement.get(2)));
                } else if ("java.lang.Float".equals(typeString)) {
                    double currentDouble = (double) currentElement.get(2);
                    float currentDeccimal = (float) currentDouble;
                    root.addChild(new AnalysisTree<>(currentKey, currentDeccimal));
                }
            }
        }
        return Result.of(root);
    }

    /**
     * Static function to convert a nested HashMap being processed by {@link YAMLAnalysisFormat} into its AnalysisTree representation.
     * This function is only suited to process a HashMap having only a single key in its first layer. Otherwise
     * an empty result will be returned.
     *
     * @param hashMap data to convert
     * @return on success returns a recursively built tree including its children wrapped in a Result<>.
     * On failure return an empty Result
     */
    public static Result<AnalysisTree<?>> yamlHashMapToTree(HashMap<String, Object> hashMap) {
        if (hashMap.size() == 1) {
            Entry<String, Object> currentEntry = hashMap.entrySet().iterator().next();
            return yamlHashMapToTree((HashMap<String, Object>) currentEntry.getValue(), currentEntry.getKey());
        } else {
            return Result.empty();
        }
    }
}
