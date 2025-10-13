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
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.identifier.AIdentifier;
import de.featjar.base.data.identifier.IIdentifiable;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.base.io.IO;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.analysis.AnalysisTree;
import de.featjar.feature.model.cli.PrintStatistics.AnalysesScope;
import de.featjar.feature.model.io.FeatureModelFormats;
import de.featjar.feature.model.io.json.JSONAnalysisFormat;
import de.featjar.feature.model.io.xml.XMLFeatureModelFormat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

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
                "printStats", "--input", "../formula/src/testFixtures/resources/Automotive02_V1/model.xml");
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
                "../formula/src/testFixtures/resources/Automotive02_V1/model.xml",
                "--output",
                "model_outputWithFileValidExtension.csv");
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
                "../formula/src/testFixtures/resources/Automotive02_V1/model.xml",
                "--output",
                "model_outputWithFileInvalidExtension.pdf");
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
                "../formula/src/testFixtures/resources/Automotive02_V1/model.xml",
                "--output",
                "model_outputWithoutFileExtension");
        assertEquals(1, exit_code);
    }

    /**
     * Testing whether collecting statistics with scope specified to ALL actually returns values for all parameters.
     */
    @Test
    void scopeAll() throws IOException {
        String content =
                "{Number of Atoms=0, Feature Density=0.0, Average Constraints=NaN, Operator Distribution={}, [Tree 1] Average Number of Children=0.0, [Tree 1] Number of Top Features=0, [Tree 1] Number of Leaf Features=1, [Tree 1] Tree Depth=1, [Tree 1] Group Distribution={AlternativeGroup=0, AndGroup=1, OtherGroup=0, OrGroup=0}}";
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
        String content = "{Number of Atoms=0, Feature Density=0.0, Average Constraints=NaN, Operator Distribution={}}";
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
    
    /**
     *  Testing whether CSV output creates correct file
     */
    @Test
    void jsonOuputTest() throws IOException {
        
    	int exit_code = FeatJAR.runTest(
                "printStats",
                "--input",
                "../formula/src/testFixtures/resources/Automotive02_V1/model.xml",
                "--output",
                "model_jsonOuputTest.json",
                "--overwrite");
        assertEquals(0, exit_code);
    	
        AnalysisTree<?> tree = IO.load(Paths.get("model_jsonOuputTest.json"), new JSONAnalysisFormat()).get();
                
        //assertEquals(tree, tree2);
        
//    	FeatJAR.initialize();
//
//        Path outputPath = Paths.get("model_csvOuputTest.xml");
//        LinkedHashMap<String, Object> dummyData = new LinkedHashMap<>(Map.of(
//        			"Value1", 67,
//        			"Value2", 4.20,
//        			"Value3", "Testing"
//        		));
//        
//        Files.deleteIfExists(outputPath);
//
//        // let program write model to XML file
//        new PrintStatistics().writeTo;
//
//        // round trip: rebuild model from XML file
//        FeatureModel retrievedModel =
//                (FeatureModel) IO.load(outputPath, new XMLFeatureModelFormat()).get();
//
//        assertEquals(model, retrievedModel);
//
//        Files.deleteIfExists(outputPath);
//        FeatJAR.deinitialize();
    	
    }
    
    /**
     *  Testing whether YAML output creates correct file
     */
    @Test
    void yamlOuputTest() throws IOException {
        
    }
}
