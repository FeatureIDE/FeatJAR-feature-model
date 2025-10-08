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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.feature.model.analysis.AnalysisTree;
import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;

public class AnalysisTreeTest {

    @Test
    public void mapToTreeTest() {
        LinkedHashMap<String, Object> emptyMap = new LinkedHashMap<String, Object>();
        AnalysisTree<?> returnedTree = AnalysisTree.hashMapToTree(emptyMap, "empty");

        assertEquals(returnedTree.getName(), "empty");
        assertEquals(returnedTree.getChildrenCount(), 0);

        emptyMap.put("intfirstLevel", 42);
        emptyMap.put("floatfirstLevel", (float) 42);
        emptyMap.put("doublefirstLevel", (double) 42);

        returnedTree = AnalysisTree.hashMapToTree(emptyMap, "valuesFirstLevel");
        assertEquals(returnedTree.getChildrenCount(), 3);
        assertTrue(returnedTree.getChild(0).isPresent());
        assertEquals(returnedTree.getChild(0).get().getName(), "intfirstLevel");
        assertEquals(returnedTree.getChild(0).get().getValue(), 42);
        assertTrue(returnedTree.getChild(1).isPresent());
        assertEquals(returnedTree.getChild(1).get().getName(), "floatfirstLevel");
        assertEquals(returnedTree.getChild(1).get().getValue(), (float) 42);
        assertTrue(returnedTree.getChild(2).isPresent());
        assertEquals(returnedTree.getChild(2).get().getName(), "doublefirstLevel");
        assertEquals(returnedTree.getChild(2).get().getValue(), (double) 42);

        LinkedHashMap<String, Object> firstLevelMap = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, Object> secondLevelMap1 = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, Object> secondLevelMap2 = new LinkedHashMap<String, Object>();

        firstLevelMap.put("intsecondLevel", 43);
        secondLevelMap1.put("intthirdLevel", 61);
        secondLevelMap2.put("float1thirdLevel", (float) 21);
        secondLevelMap2.put("float2thirdLevel", (float) 22);

        firstLevelMap.put("map1secondLevel", secondLevelMap1);
        firstLevelMap.put("map2secondLevel", secondLevelMap2);
        emptyMap.put("mapfirstLevel", firstLevelMap);

        returnedTree = AnalysisTree.hashMapToTree(emptyMap, "nestedMaps");

        assertEquals(returnedTree.getChildrenCount(), 4);
        AnalysisTree<?> mapfirstLevel = returnedTree.getChild(3).get();
        assertEquals(mapfirstLevel.getChildrenCount(), 3);
        assertEquals(mapfirstLevel.getName(), "mapfirstLevel");
        assertEquals(mapfirstLevel.getValue(), null);
        assertEquals(mapfirstLevel.getChild(0).get().getName(), "intsecondLevel");
        assertEquals(mapfirstLevel.getChild(0).get().getValue(), 43);

        AnalysisTree<?> map1secondLevel = mapfirstLevel.getChild(1).get();
        AnalysisTree<?> map2secondLevel = mapfirstLevel.getChild(2).get();

        assertEquals(map1secondLevel.getName(), "map1secondLevel");
        assertEquals(map1secondLevel.getValue(), null);
        assertEquals(map1secondLevel.getChildrenCount(), 1);
        assertEquals(map1secondLevel.getChild(0).get().getName(), "intthirdLevel");
        assertEquals(map1secondLevel.getChild(0).get().getValue(), 61);

        assertEquals(map2secondLevel.getName(), "map2secondLevel");
        assertEquals(map2secondLevel.getValue(), null);
        assertEquals(map2secondLevel.getChildrenCount(), 2);
        assertEquals(map2secondLevel.getChild(0).get().getName(), "float1thirdLevel");
        assertEquals(map2secondLevel.getChild(0).get().getValue(), (float) 21);
        assertEquals(map2secondLevel.getChild(1).get().getName(), "float2thirdLevel");
        assertEquals(map2secondLevel.getChild(1).get().getValue(), (float) 22);
    }
}
