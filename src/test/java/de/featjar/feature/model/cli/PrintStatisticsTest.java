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
import java.io.IOException;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AIdentifier} and {@link IIdentifiable}.
 *
 * @author Knut & Kilian
 */
public class PrintStatisticsTest {

    PrintStatistics printStats = new PrintStatistics();

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
    void printPretty() throws IOException {}

    @Test
    void printDefault() throws IOException {}

    @Test
    void scopeAll() throws IOException {}

    @Test
    void scopeTreeRelated() throws IOException {}

    @Test
    void scopeConstraintRelated() throws IOException {}

    @Test
    void scopeNotSpecified() throws IOException {}
}
