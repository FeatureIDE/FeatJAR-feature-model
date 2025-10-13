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
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.cli.PrintStatistics.AnalysesScope;
import java.util.LinkedHashMap;
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

    @Test
    void inputTest() {

        int exit_code = FeatJAR.runTest(
                "printStats", "--input", "../formula/src/testFixtures/resources/Automotive02_V1/model.xml");
        assertEquals(0, exit_code);
    }

    @Test
    void noInput() {

        assertEquals(1, FeatJAR.runTest("printStats", "--input"));
        assertEquals(1, FeatJAR.runTest("printStats"));
    }

    @Test
    void outputWithFileValidExtension() {

        int exit_code = FeatJAR.runTest(
                "printStats",
                "--input",
                "../formula/src/testFixtures/resources/Automotive02_V1/model.xml",
                "--output",
                "desktop/folder/model.xml");
        assertEquals(0, exit_code);
    }

    @Test
    void outputWithFileInvalidExtension() {

        int exit_code = FeatJAR.runTest(
                "printStats",
                "--input",
                "../formula/src/testFixtures/resources/Automotive02_V1/model.xml",
                "--output",
                "desktop/folder/model.pdf");
        assertEquals(1, exit_code);
    }

    @Test
    void outputWithoutFileExtension() {

        int exit_code = FeatJAR.runTest(
                "printStats",
                "--input",
                "../formula/src/testFixtures/resources/Automotive02_V1/model.xml",
                "--output",
                "desktop/folder");
        assertEquals(1, exit_code);
    }

    @Test
    void scopeAll() {
        String content =
                "{Number of Atoms=0, Feature Density=0.0, Average Constraints=NaN, Operator Distribution={}, [Tree 1] Average Number of Children=0.0, [Tree 1] Number of Top Features=0, [Tree 1] Number of Leaf Features=1, [Tree 1] Tree Depth=1, [Tree 1] Group Distribution={AlternativeGroup=0, AndGroup=1, OtherGroup=0, OrGroup=0}}";
        String comparison =
                printStats.collectStats(minimalModel, AnalysesScope.ALL).toString();
        assertEquals(content, comparison);
    }

    @Test
    void scopeTreeRelated() {

        String content =
                "{[Tree 1] Average Number of Children=0.0, [Tree 1] Number of Top Features=0, [Tree 1] Number of Leaf Features=1, [Tree 1] Tree Depth=1, [Tree 1] Group Distribution={AlternativeGroup=0, AndGroup=1, OtherGroup=0, OrGroup=0}}";
        String comparison = printStats
                .collectStats(minimalModel, AnalysesScope.TREE_RELATED)
                .toString();
        assertEquals(content, comparison);
    }

    @Test
    void scopeConstraintRelated() {
        String content = "{Number of Atoms=0, Feature Density=0.0, Average Constraints=NaN, Operator Distribution={}}";
        String comparison = printStats
                .collectStats(minimalModel, AnalysesScope.CONSTRAINT_RELATED)
                .toString();
        assertEquals(content, comparison);
    }

    @Test
    void prettyStringBuilder() {

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
                + "	   Nested Entry 1                : 5\n"
                + "	   Nested Entry 2                : 6\n"
                + "\n"
                + "		CONSTRAINT RELATED STATS\n"
                + "                 \n"
                + "Number of Atoms                          : \n"
                + "\n"
                + "		TREE RELATED STATS\n"
                + "                       \n"
                + "[Tree 1] Average Number of Children      : \n";

        assertEquals(comparison, printStats.buildStringPrettyStats(testData).toString());
    }
}
