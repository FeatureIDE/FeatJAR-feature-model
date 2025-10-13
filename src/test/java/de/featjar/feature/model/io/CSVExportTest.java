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

import de.featjar.base.io.IO;
import de.featjar.feature.model.analysis.AnalysisTree;
import de.featjar.feature.model.io.csv.CSVAnalysisFormat;
import java.io.IOException;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class CSVExportTest {

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
    public void CSVTest() throws IOException {
        CSVAnalysisFormat csvAnalysisFormat = new CSVAnalysisFormat();
        String csvString = csvAnalysisFormat.serialize(createDefaultTree()).orElseThrow();
        IO.save(createDefaultTree(), Paths.get("file.csv"), csvAnalysisFormat);
        assertEquals(
                csvString,
                "AnalysisType;Name;Class;Value\n"
                + "Analysis;numOfLeafFeatures;java.lang.Float;12.4\n"
                + "Analysis;numOfTopFeatures;java.lang.Double;3.3\n"
                + "Analysis;treeDepth;java.lang.Integer;3\n"
                + "Analysis;avgNumOfChildren;java.lang.Integer;3\n"
                + "Analysis;numInOrGroups;java.lang.Integer;7\n"
                + "Analysis;numInAltGroups;java.lang.Integer;5\n"
                + "Analysis;numOfAtoms;java.lang.Integer;8\n"
                + "Analysis;avgNumOfAsss;java.lang.Integer;4\n"
                + "avgNumOfAtomsPerConstraints;test property;java.lang.Double;3.3\n"
                + "avgNumOfAtomsPerConstraints;numOfLeafFeatures;java.lang.Float;12.4\n");
    }
}
