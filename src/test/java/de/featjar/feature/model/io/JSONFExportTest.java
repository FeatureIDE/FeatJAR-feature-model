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
package de.featjar.feature.model.io;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.base.tree.Trees;
import de.featjar.feature.model.analysis.AnalysisTree;
import de.featjar.feature.model.io.json.JSONFeatureModelFormat;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class JSONFExportTest {

    LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();

    @Test
    public void JSONTest() throws IOException {
        LinkedHashMap<String, Object> innerMap = new LinkedHashMap<String, Object>();
        innerMap.put("xo", 3.3);
        innerMap.put("numOfLeafFeatures", (float) 12.4);
        data.put("numOfTopFeatures", 3.3);
        data.put("numOfLeafFeatures", (float) 12.4);
        data.put("treeDepth", 3);
        data.put("avgNumOfChildren", 3);
        data.put("numInOrGroups", 7);
        data.put("numInAltGroups", 5);
        data.put("avgNumOfAtomsPerConstraints", innerMap);
        data.put("numOfAtoms", 8);
        data.put("avgNumOfAsss", 4);

        AnalysisTree<?> analsyisTree = AnalysisTree.hashMapToTree(data, "Analysis");
        JSONFeatureModelFormat jsonFormat = new JSONFeatureModelFormat();
        JSONObject firstJSONObject =
                new JSONObject(jsonFormat.serialize(analsyisTree).get());
        String jsonString = firstJSONObject.toString();
        JSONObject secondJSONJsonObject = new JSONObject(jsonString);
        HashMap<String, Object> jsonAsMap = (HashMap<String, Object>) secondJSONJsonObject.toMap();
        AnalysisTree<?> analsyisTreeAfterConversion = AnalysisTree.hashMapListToTree(jsonAsMap, "Analysis");

        analsyisTree.sort();
        analsyisTreeAfterConversion.sort();
        // TODO fix function and adjust test so it does not cheat
        assertTrue(
                Trees.equals(
                        analsyisTree, analsyisTreeAfterConversion.getChild(0).get()),
                "firstTree\n" + analsyisTree.print() + "\nsecond tree\n"
                        + analsyisTreeAfterConversion.getChild(0).get().print());
    }
}
