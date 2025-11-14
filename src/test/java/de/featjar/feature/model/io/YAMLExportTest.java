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

import de.featjar.base.io.IO;
import de.featjar.base.tree.Trees;
import de.featjar.feature.model.TestDataProvider;
import de.featjar.feature.model.analysis.AnalysisTree;
import de.featjar.feature.model.io.transformer.AnalysisTreeTransformer;
import de.featjar.feature.model.io.yaml.YAMLAnalysisFormat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

public class YAMLExportTest {
    @Test
    public void YAMLTest() throws IOException {
        AnalysisTree<?> analysisTree = TestDataProvider.createSmallAnalysisTree();
        IO.save(analysisTree, Paths.get("filename.yaml"), new YAMLAnalysisFormat());
        AnalysisTree<?> outputAnalysisTree =
                IO.load(Paths.get("filename.yaml"), new YAMLAnalysisFormat()).get();
        analysisTree.sort();
        outputAnalysisTree.sort();
        assertTrue(
                Trees.equals(analysisTree, outputAnalysisTree),
                "firstTree\n" + analysisTree.print() + "\nsecond tree\n" + outputAnalysisTree.print());
        Files.deleteIfExists(Paths.get("filename.yaml"));
    }

    @Test
    public void YAMLSerialize() throws IOException {
        AnalysisTree<?> analsyisTree = TestDataProvider.createSmallAnalysisTree();
        YAMLAnalysisFormat yamlFormat = new YAMLAnalysisFormat();
        String output = yamlFormat.serialize(analsyisTree).get();

        Yaml yaml2 = new Yaml();
        Object loadedObject = yaml2.load(output);

        Map<String, Object> map = (Map<String, Object>) loadedObject;
        HashMap<String, Object> loadedHashMap = (HashMap<String, Object>) map;

        AnalysisTree<?> analysisTreeAfterConversion =
                AnalysisTreeTransformer.yamlHashMapToTree(loadedHashMap).get();

        analsyisTree.sort();
        analysisTreeAfterConversion.sort();
        assertTrue(
                Trees.equals(analsyisTree, analysisTreeAfterConversion),
                "firstTree\n" + analsyisTree.print() + "\nsecond tree\n" + analysisTreeAfterConversion.print());
        AnalysisTree<?> manualAnalysisTree = TestDataProvider.createSmallAnalysisTree();
        manualAnalysisTree.sort();
        assertTrue(
                Trees.equals(manualAnalysisTree, analysisTreeAfterConversion),
                "firstTree\n" + manualAnalysisTree.print() + "\nsecond tree\n" + analysisTreeAfterConversion.print());
    }
}
