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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.identifier.AIdentifier;
import de.featjar.base.data.identifier.IIdentifiable;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AIdentifier} and {@link IIdentifiable}.
 *
 * @author Knut & Kilian
 */
public class FormatConversionTest {

    FormatConversion formatConversion = new FormatConversion();

    @Test
    void fileWritingTest() {

        String pathToOutPutModel = "output_model.xml";
        String pathToInputModel = "../formula/src/testFixtures/resources/Automotive02_V1/model.xml";

        int exit_code = FeatJAR.runTest("formatConversion", "--input", pathToInputModel, "--output", pathToOutPutModel);
        assertEquals(0, exit_code);
        assertTrue(new File(pathToOutPutModel).exists());
        Path pathToBeDeleted = Paths.get(pathToOutPutModel);
        assertDoesNotThrow(() -> {
            Files.deleteIfExists(pathToBeDeleted);
        });
    }

    @Test
    void invalidOutput() {

        String pathToOutPutModel = "output_model.pdf";
        String pathToInputModel = "../formula/src/testFixtures/resources/Automotive02_V1/model.xml";

        int exit_code = FeatJAR.runTest("formatConversion", "--input", pathToInputModel, "--output", pathToOutPutModel);
        assertEquals(1, exit_code);
    }

    @Test
    void invalidInput() {

        String pathToOutPutModel = "output_model.xml";
        String pathToInputModel = "../formula/src/testFixtures/resources/Automotive02_V1/model.pdf";

        int exit_code = FeatJAR.runTest("formatConversion", "--input", pathToInputModel, "--output", pathToOutPutModel);
        assertEquals(1, exit_code);
    }

    @Test
    void invalid() {

        String pathToOutPutModel = "output_model.xml";
        String pathToInputModel = "../formula/src/testFixtures/resources/Automotive02_V1/model.pdf";

        int exit_code = FeatJAR.runTest("formatConversion", "--input", pathToInputModel, "--output", pathToOutPutModel);
        assertEquals(1, exit_code);
    }

    @Test
    void ioExceptionTest() {
        int exit_code = formatConversion.saveFile(Paths.get(""), null, "xml");
        System.out.println(exit_code);
        assertEquals(2, exit_code);
    }
}
