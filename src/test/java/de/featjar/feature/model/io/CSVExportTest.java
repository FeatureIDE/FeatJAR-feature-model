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

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.featjar.feature.model.analysis.AnalysisTree;
import de.featjar.feature.model.io.csv.CSVAnalysisFormat;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class CSVExportTest {

    public AnalysisTree<?> createDefaultTree() {
        AnalysisTree<?> innereanalysisTree = new AnalysisTree<>(
                "avgNumOfAtomsPerConstraints",
                new AnalysisTree<>("xo", 3.3),
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
    public void CSVTest() throws IOException {
        CSVAnalysisFormat csvAnalysisFormat = new CSVAnalysisFormat();
        String csvString = csvAnalysisFormat.serialize(createDefaultTree()).orElseThrow();
        assertEquals(
                csvString,
                "AnalysisType;Name;Value;Class\n"
                        + "Analysis;numOfLeafFeatures;12.4;java.lang.Float\n"
                        + "Analysis;numOfTopFeatures;3.3;java.lang.Double\n"
                        + "Analysis;treeDepth;3;java.lang.Integer\n"
                        + "Analysis;avgNumOfChildren;3;java.lang.Integer\n"
                        + "Analysis;numInOrGroups;7;java.lang.Integer\n"
                        + "Analysis;numInAltGroups;5;java.lang.Integer\n"
                        + "Analysis;numOfAtoms;8;java.lang.Integer\n"
                        + "Analysis;avgNumOfAsss;4;java.lang.Integer\n"
                        + "avgNumOfAtomsPerConstraints;xo;3.3;java.lang.Double\n"
                        + "avgNumOfAtomsPerConstraints;numOfLeafFeatures;12.4;java.lang.Float\n");
    }
}
