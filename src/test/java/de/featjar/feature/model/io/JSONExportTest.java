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

import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.base.tree.Trees;
import de.featjar.feature.model.analysis.AnalysisTree;
import de.featjar.feature.model.io.json.JSONAnalysisFormat;
import de.featjar.feature.model.io.transformer.AnalysisTreeTransformer;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class JSONExportTest {

    LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();

    public AnalysisTree<?> createDefaultTree() {
        AnalysisTree<?> innereanalysisTree = new AnalysisTree<>(
                "avgNumOfAtomsPerConstraints",
                new AnalysisTree<>("test property", 3.3),
                new AnalysisTree<>("numOfLeafFeatures", (float) 12.4));

        AnalysisTree<?> analysisTree = new AnalysisTree<>(
                "Analysis",
                new AnalysisTree<>("numOfLeafFeatures", (float) 12.4),
                new AnalysisTree<>("numOfTopFeatures", 3.3),
                new AnalysisTree<>("treeDepth", 3),
                new AnalysisTree<>("avgNumOfChildren", 3),
                new AnalysisTree<>("numInOrGroups", 7),
                new AnalysisTree<>("numInAltGroups", 5),
                new AnalysisTree<>("numOfAtoms", 8),
                new AnalysisTree<>("avgNumOfAsss", 4),
                innereanalysisTree);
        return analysisTree;
    }

    @Test
    public void JSONSerialize() throws IOException {
        LinkedHashMap<String, Object> innerMap = new LinkedHashMap<String, Object>();
        innerMap.put("test property", 3.3);
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

        Result<AnalysisTree<?>> analsyisTreeResult = AnalysisTreeTransformer.hashMapToTree(data, "Analysis");
        AnalysisTree<?> analsyisTree = analsyisTreeResult.get();
        JSONAnalysisFormat jsonFormat = new JSONAnalysisFormat();
        JSONObject firstJSONObject =
                new JSONObject(jsonFormat.serialize(analsyisTree).get());
        String jsonString = firstJSONObject.toString();
        JSONObject secondJSONObject = new JSONObject(jsonString);
        HashMap<String, Object> jsonAsMap = (HashMap<String, Object>) secondJSONObject.toMap();
        AnalysisTree<?> analsyisTreeAfterConversion =
                AnalysisTreeTransformer.jsonHashMapToTree(jsonAsMap).get();

        analsyisTree.sort();
        analsyisTreeAfterConversion.sort();
        assertTrue(
                Trees.equals(analsyisTree, analsyisTreeAfterConversion),
                "firstTree\n" + analsyisTree.print() + "\nsecond tree\n" + analsyisTreeAfterConversion.print());
        AnalysisTree<?> manualAnalysisTree = createDefaultTree();
        manualAnalysisTree.sort();
        assertTrue(
                Trees.equals(manualAnalysisTree, analsyisTreeAfterConversion),
                "firstTree\n" + manualAnalysisTree.print() + "\nsecond tree\n" + analsyisTreeAfterConversion.print());
    }

    @Test
    public void JSONSaveLoadTest() throws IOException {
        AnalysisTree<?> analysisTree = createDefaultTree();
        IO.save(analysisTree, Paths.get("filename.json"), new JSONAnalysisFormat());
        AnalysisTree<?> outputAnalysisTree =
                IO.load(Paths.get("filename.json"), new JSONAnalysisFormat()).get();
        analysisTree.sort();
        outputAnalysisTree.sort();
        assertTrue(
                Trees.equals(analysisTree, outputAnalysisTree),
                "firstTree\n" + analysisTree.print() + "\nsecond tree\n" + outputAnalysisTree.print());
    }
}
