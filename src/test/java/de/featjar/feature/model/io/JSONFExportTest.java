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

import de.featjar.base.io.IO;
import de.featjar.base.tree.Trees;
import de.featjar.base.tree.visitor.TreePrinter;
import de.featjar.feature.model.analysis.AnalysisTree;
import de.featjar.feature.model.io.json.JSONFeatureModelFormat;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
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
        System.out.println("Tree input" + Trees.traverse(analsyisTree, new TreePrinter()));

        FileSystem fileSystem = FileSystems.getDefault();
        JSONFeatureModelFormat jsonFormat = new JSONFeatureModelFormat();
        System.out.println(
                "Tree as json String: \n" + jsonFormat.serialize(analsyisTree).get());
        System.out.println("Tree as json String END \n");
        try {
            FileOutputStream outputStream = new FileOutputStream("filename.json");
            IO.save(analsyisTree, Paths.get("filename.json"), new JSONFeatureModelFormat());
            System.out.println("no error");
        } catch (Exception e) {
            System.out.println(e);
        }

        System.out.println(analsyisTree.getChild(1).get().getName());

        // JSONObject jsonobj = new JSONObject(data);
        // System.out.println(jsonobj.toString(1));
        // JSONObject jsonobj1 = new JSONObject(jsonobj.toString(1));
        // for (Iterator iterator = jsonobj1.keys(); iterator.hasNext();) {
        //	System.out.println(jsonobj1.get(iterator.next().toString()).getClass());

        // }

        // System.out.println("Tree input" + Trees.traverse(analsyisTree, new TreePrinter()));

        // AnalysisTree<?> analysisTreeLoaded = IO.load(Paths.get("filename.json"), new JSONFeatureModelFormat()).get();
        // AnalysisTree<?> analysisTreeLoaded = IO.load(Paths.get("filename.json"), new JSONFeatureModelFormat()).get();
        String AnalysisListJson = jsonFormat.serialize(analsyisTree).get();
        JSONObject jsonobj = new JSONObject(AnalysisListJson);
        AnalysisTree<?> analysisTreeLoaded =
                AnalysisTree.hashMapListToTree((HashMap<String, Object>) jsonobj.toMap(), "Analysis");
        System.out.println(jsonobj.toString());

        // AnalysisTree<?> analysisTreeLoaded = IO.load(Paths.get("filename.json"), new JSONFeatureModelFormat()).get();

        System.out.println("Tree output"
                + Trees.traverse(analysisTreeLoaded, new TreePrinter()).get());
    }
}
