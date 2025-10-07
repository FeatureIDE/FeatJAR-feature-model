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
import java.util.Iterator;
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

        FileSystem fileSystem = FileSystems.getDefault();
        JSONFeatureModelFormat jsonFormat = new JSONFeatureModelFormat();
        System.out.println(jsonFormat.serialize(data).get());
        try {
            FileOutputStream outputStream = new FileOutputStream("filename.json");
            IO.save(data, Paths.get("filename.json"), new JSONFeatureModelFormat());
            System.out.println("no error");
        } catch (Exception e) {
            System.out.println(e);
        }

        JSONObject jsonobj = new JSONObject(data);
        System.out.println(jsonobj.toString(1));
        JSONObject jsonobj1 = new JSONObject(jsonobj.toString(1));
        // for (Iterator iterator = jsonobj1.keys(); iterator.hasNext();) {
        //	System.out.println(jsonobj1.get(iterator.next().toString()).getClass());

        // }

        for (Iterator<String> iterator = data.keySet().iterator(); iterator.hasNext(); ) {
            String currentKey = iterator.next();
            System.out.println((data.get(currentKey)).getClass().isPrimitive());
            // System.out.println((data.get(iterator.next())).getClass());
        }

        AnalysisTree analsyisTree = AnalysisTree.hashMapToTree(data, "Analysis");

        System.out.println(Trees.traverse(analsyisTree, new TreePrinter()).get());
    }
}
