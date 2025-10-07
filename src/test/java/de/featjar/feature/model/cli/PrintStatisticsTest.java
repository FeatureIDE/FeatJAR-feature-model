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
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.cli.PrintStatistics.AnalysesScope;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    FeatureModel minimalModel = generateMinimalModel();

    private FeatureModel generateMinimalModel() {
        FeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
        featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
        return featureModel;
    }

    public static int indexOfDifference(String s1, String s2) {
        int minLen = Math.min(s1.length(), s2.length());
        for (int i = 0; i < minLen; i++) {
            if (s1.charAt(i) != s2.charAt(i)) {
                return i; // index where difference starts
            }
        }
        if (s1.length() == s2.length()) {
            return -1; // strings are equal
        } else {
            return minLen; // difference due to length
        }
    }


    @Test
    void inputTest() throws IOException {

        int exit_code = FeatJAR.runTest(
                "printStats", "--input", "../formula/src/testFixtures/resources/Automotive02_V1/model.xml");
        assertEquals(0, exit_code);
    }

    @Test
    void noInput() throws IOException {

        assertEquals(1, FeatJAR.runTest("printStats", "--input"));
        assertEquals(1, FeatJAR.runTest("printStats"));
    }

    @Test
    void outputWithFileValidExtension() throws IOException {

        int exit_code = FeatJAR.runTest(
                "printStats",
                "--input",
                "../formula/src/testFixtures/resources/Automotive02_V1/model.xml",
                "--output",
                "desktop/folder/model.xml");
        assertEquals(0, exit_code);
    }

    @Test
    void outputWithFileInvalidExtension() throws IOException {

        int exit_code = FeatJAR.runTest(
                "printStats",
                "--input",
                "../formula/src/testFixtures/resources/Automotive02_V1/model.xml",
                "--output",
                "desktop/folder/model.pdf");
        assertEquals(1, exit_code);
    }

    @Test
    void outputWithoutFileExtension() throws IOException {

        int exit_code = FeatJAR.runTest(
                "printStats",
                "--input",
                "../formula/src/testFixtures/resources/Automotive02_V1/model.xml",
                "--output",
                "desktop/folder");
        assertEquals(1, exit_code);
    }
    
    @Test
    void scopeAll() throws IOException {
    	String content = "{Number of Atoms=0, Feature Density=0.0, Average Constraints=NaN, Operator Distribution={}, [Tree 1] Average Number of Childen=0.0, [Tree 1] Number of Top Features=0, [Tree 1] Number of Leaf Features=1, [Tree 1] Tree Depth=1, [Tree 1] Group Distribution={AlternativeGroup=0, AndGroup=1, OtherGroup=0, OrGroup=0}}";
    	String comparison = printStats.collectStats(minimalModel, AnalysesScope.ALL).toString();
    	assertEquals(content, comparison);
    	
    }

    @Test
    void scopeTreeRelated() throws IOException {
    	String content = "{[Tree 1] Average Number of Childen=0.0, [Tree 1] Number of Top Features=0, [Tree 1] Number of Leaf Features=1, [Tree 1] Tree Depth=1, [Tree 1] Group Distribution={AlternativeGroup=0, AndGroup=1, OtherGroup=0, OrGroup=0}}";
    	String comparison = printStats.collectStats(minimalModel, AnalysesScope.TREE_RELATED).toString();
    	assertEquals(content, comparison);
    }

    @Test
    void scopeConstraintRelated() throws IOException {
    	String content = "{Number of Atoms=0, Feature Density=0.0, Average Constraints=NaN, Operator Distribution={}}";
    	String comparison = printStats.collectStats(minimalModel, AnalysesScope.CONSTRAINT_RELATED).toString();
    	assertEquals(content, comparison);
    }
    
    @Test
    void prettyStringBuilder() throws IOException {
    	
    	LinkedHashMap<String, Object> testData = new LinkedHashMap<>();
    	testData.put("Normal Entry", 10);
    	// LinkedHashMap<String, Object> nestedMap = new LinkedHashMap<>();
    	// nestedMap.put("Nested Entry 1", 5);
    	// nestedMap.put("Nested Entry 2", 6);
    	// testData.put("HashMap Entry",  nestedMap);
    	// testData.put("Number of Atoms", "");
    	// testData.put("[Tree 1] Average Number of Childen", "");
    	
    	StringBuilder comparison = new StringBuilder();
    	comparison.append("Normal Entry                             : 10\n");
    	
    	System.out.println(printStats.buildStringPrettyStats(testData));
    	
    	assertEquals(printStats.buildStringPrettyStats(testData), comparison);

    }
}
