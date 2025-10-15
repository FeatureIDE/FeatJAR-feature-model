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
package de.featjar.feature.model.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.identifier.AIdentifier;
import de.featjar.base.data.identifier.IIdentifiable;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.base.io.IO;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.analysis.AnalysisTree;
import de.featjar.feature.model.cli.PrintStatistics.AnalysesScope;
import de.featjar.feature.model.io.json.JSONAnalysisFormat;
import de.featjar.feature.model.io.yaml.YAMLAnalysisFormat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AIdentifier} and {@link IIdentifiable}.
 *
 * @author Knut & Kilian
 */
public class PrintStatisticsTest {

    PrintStatistics printStats = new PrintStatistics();
    FeatureModel minimalModel = generateModel();

    private FeatureModel generateModel() {
        FeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
        featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
        return featureModel;
    }

    /**
     *  Testing the parsing of an input model.
     */
    @Test
    void inputTest() throws IOException {

        int exit_code = FeatJAR.runTest(
                "printStats", "--input", "src/test/java/de/featjar/feature/model/cli/resources/simpleTestModel.xml");
        assertEquals(0, exit_code);
    }

    /**
     *  Testing with no input, can't gather statistics without source model.
     */
    @Test
    void noInput() throws IOException {

        assertEquals(1, FeatJAR.runTest("printStats", "--input"));
        assertEquals(1, FeatJAR.runTest("printStats"));
    }

    /**
     *  Testing input with valid file extension (valid extensions currently are csv, yaml and json).
     */
    @Test
    void outputWithFileValidExtension() throws IOException {

        int exit_code = FeatJAR.runTest(
                "printStats",
                "--input",
                "src/test/java/de/featjar/feature/model/cli/resources/simpleTestModel.xml",
                "--output",
                "model_outputWithFileValidExtension.csv",
                "--overwrite");
        assertEquals(0, exit_code);
        Files.deleteIfExists(Paths.get("model_outputWithFileValidExtension.csv"));
    }

    /**
     *  Testing with a file extension for which there is no analysis format defined.
     */
    @Test
    void outputWithFileInvalidExtension() throws IOException {

        int exit_code = FeatJAR.runTest(
                "printStats",
                "--input",
                "src/test/java/de/featjar/feature/model/cli/resources/simpleTestModel.xml",
                "--output",
                "model_outputWithFileInvalidExtension.pdf",
                "--overwrite");
        assertEquals(1, exit_code);
    }

    /**
     *  Testing without any (legal or not) file extension specified.
     */
    @Test
    void outputWithoutFileExtension() throws IOException {

        int exit_code = FeatJAR.runTest(
                "printStats",
                "--input",
                "src/test/java/de/featjar/feature/model/cli/resources/simpleTestModel.xml",
                "--output",
                "model_outputWithoutFileExtension",
                "--overwrite");
        assertEquals(1, exit_code);
    }

    /**
     * Testing whether collecting statistics with scope specified to ALL actually returns values for all parameters.
     */
    @Test
    void scopeAll() throws IOException {
        String content =
                "{Number of Atoms=0, Feature Density=0.0, Average Constraints=NaN, [Tree 1] Average Number of Children=0.0, [Tree 1] Number of Top Features=0, [Tree 1] Number of Leaf Features=1, [Tree 1] Tree Depth=1, [Tree 1] Group Distribution={AlternativeGroup=0, AndGroup=1, OtherGroup=0, OrGroup=0}}";
        String comparison =
                printStats.collectStats(minimalModel, AnalysesScope.ALL).toString();
        assertEquals(content, comparison);
    }

    /**
     *  Testing whether collecting statistics with scope specified to TREE_RELATED actually returns values for tree related parameters only.
     */
    @Test
    void scopeTreeRelated() throws IOException {

        String content =
                "{[Tree 1] Average Number of Children=0.0, [Tree 1] Number of Top Features=0, [Tree 1] Number of Leaf Features=1, [Tree 1] Tree Depth=1, [Tree 1] Group Distribution={AlternativeGroup=0, AndGroup=1, OtherGroup=0, OrGroup=0}}";
        String comparison = printStats
                .collectStats(minimalModel, AnalysesScope.TREE_RELATED)
                .toString();
        assertEquals(content, comparison);
    }

    /**
     *  Testing whether collecting statistics with scope specified to CONSTRAINT_RELATED actually returns values for constraint related parameters only.
     */
    @Test
    void scopeConstraintRelated() throws IOException {
        String content = "{Number of Atoms=0, Feature Density=0.0, Average Constraints=NaN}" + "";
        String comparison = printStats
                .collectStats(minimalModel, AnalysesScope.CONSTRAINT_RELATED)
                .toString();

        assertEquals(content, comparison);
    }

    /**
     *  Testing whether using the flag PRETTY actually returns expected string.
     */
    @Test
    void prettyStringBuilder() throws IOException {

        LinkedHashMap<String, Object> testData = new LinkedHashMap<>();
        testData.put("Normal Entry", 10);
        LinkedHashMap<String, Object> nestedMap = new LinkedHashMap<>();
        nestedMap.put("Nested Entry 1", 5);
        nestedMap.put("Nested Entry 2", 6);
        testData.put("HashMap Entry", nestedMap);
        testData.put("Number of Atoms", "");
        testData.put("[Tree 1] Average Number of Children", "");

        String comparison = "Normal Entry                             : 10\n"
                + "HashMap Entry                           \n"
                + "           Nested Entry 1                : 5\n"
                + "           Nested Entry 2                : 6\n"
                + "\n"
                + "                CONSTRAINT RELATED STATS\n"
                + "                 \n"
                + "Number of Atoms                          : \n"
                + "\n"
                + "                TREE RELATED STATS\n"
                + "                       \n"
                + "[Tree 1] Average Number of Children      : \n"
                + "";

        assertEquals(comparison, printStats.buildStringPrettyStats(testData).toString());
    }
    // TODO implement this test once the jsonHashMapToTree() function works in AnalysisTreeTransformer
    /**
     *  Testing whether JSON output creates correct file
     */
    @Test
    void jsonOuputTest() throws IOException {

        int exit_code = FeatJAR.runTest(
                "printStats",
                "--input",
                "src/test/java/de/featjar/feature/model/cli/resources/simpleTestModel.xml",
                "--output",
                "model_jsonOuputTest.json",
                "--overwrite");

        AnalysisTree<?> tree = IO.load(Paths.get("model_jsonOuputTest.json"), new JSONAnalysisFormat())
                .get();
        AnalysisTree<?> tree_expected = IO.load(
                        Paths.get("src/test/java/de/featjar/feature/model/cli/resources/expected_jsonOutputTest.json"),
                        new JSONAnalysisFormat())
                .get();

        assertEquals(tree.print(), tree_expected.print());
        assertEquals(0, exit_code);

        Files.deleteIfExists(Paths.get("model_jsonOutputTest.json"));
    }

    /**
     *  Testing whether YAML output creates correct file
     */
    @Test
    void yamlOutputTest() throws IOException {

        int exit_code = FeatJAR.runTest(
                "printStats",
                "--input",
                "src/test/java/de/featjar/feature/model/cli/resources/simpleTestModel.xml",
                "--output",
                "model_yamlOuputTest.yaml",
                "--overwrite");

        AnalysisTree<?> tree = IO.load(Paths.get("model_yamlOuputTest.yaml"), new YAMLAnalysisFormat())
                .get();
        AnalysisTree<?> tree_expected = IO.load(
                        Paths.get("src/test/java/de/featjar/feature/model/cli/resources/expected_yamlOuputTest.yaml"),
                        new YAMLAnalysisFormat())
                .get();

        assertEquals(tree.print(), tree_expected.print());
        assertEquals(0, exit_code);

        Files.deleteIfExists(Paths.get("model_yamlOuputTest.yaml"));
    }

    /**
     *  Testing whether csv output creates correct file
     */
    @Test
    void csvOutputTest() throws IOException {

        int exit_code = FeatJAR.runTest(
                "printStats",
                "--input",
                "src/test/java/de/featjar/feature/model/cli/resources/simpleTestModel.xml",
                "--output",
                "model_csvOuputTest.csv",
                "--overwrite");

        // CSVAnaylsisFormat() does not support parsing, so this test uses java.nio.file.Files
        String content = Files.readString(Paths.get("model_csvOuputTest.csv"));

        String expected = "AnalysisType;Name;Class;Value\n"
                + "csv;Number of Atoms;java.lang.Integer;1\n"
                + "csv;Feature Density;java.lang.Float;0.33333334\n"
                + "csv;Average Constraints;java.lang.Float;1.0\n"
                + "csv;[Tree 1] Average Number of Children;java.lang.Double;0.6666666666666666\n"
                + "csv;[Tree 1] Number of Top Features;java.lang.Integer;2\n"
                + "csv;[Tree 1] Number of Leaf Features;java.lang.Integer;2\n"
                + "csv;[Tree 1] Tree Depth;java.lang.Integer;2\n"
                + "[Tree 1] Group Distribution;AlternativeGroup;java.lang.Integer;0\n"
                + "[Tree 1] Group Distribution;AndGroup;java.lang.Integer;3\n"
                + "[Tree 1] Group Distribution;OtherGroup;java.lang.Integer;0\n"
                + "[Tree 1] Group Distribution;OrGroup;java.lang.Integer;0\n";

        assertEquals(expected, content);
        assertEquals(0, exit_code);

        Files.deleteIfExists(Paths.get("model_csvOuputTest.csv"));
    }
}
