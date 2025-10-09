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
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.base.io.IO;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.io.xml.XMLFeatureModelFormat;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AIdentifier} and {@link IIdentifiable}.
 *
 * @author Knut, Kilian & Benjamin
 */
public class FormatConversionTest {

    private FeatureModel generateMinimalModel() {
        FeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
        featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
        return featureModel;
    }

    /**
     * Tests whether an XML file can be loaded and written elsewhere (no content check)
     */
    @Test
    void fileWritingTest() {
        String pathToOutPutModel = "../../Desktop/modelWritingTest.xml";
        String pathToInputModel = "../formula/src/testFixtures/resources/Automotive02_V1/model.xml";

        int exit_code = FeatJAR.runTest("formatConversion", "--input", pathToInputModel, "--output", pathToOutPutModel);
        assertEquals(0, exit_code);
        assertTrue(new File(pathToOutPutModel).exists());

        Path pathToBeDeleted = Paths.get(pathToOutPutModel);
        assertDoesNotThrow(() -> {
            Files.deleteIfExists(pathToBeDeleted);
        });
    }

    /**
     * Attempts to write model to an incompatible file format (.pdf) and checks whether it's rejected correctly.
     */
    @Test
    void invalidOutput() {

        String pathToOutPutModel = "output_model.pdf";
        String pathToInputModel = "../formula/src/testFixtures/resources/Automotive02_V1/model.xml";

        int exit_code = FeatJAR.runTest("formatConversion", "--input", pathToInputModel, "--output", pathToOutPutModel);
        assertEquals(2, exit_code);
    }

    /**
     * Attempts to read model from an incompatible file format (.pdf) and checks whether it's rejected correctly.
     */
    @Test
    void invalidInput() {

        String pathToOutPutModel = "output_model.xml";
        String pathToInputModel = "../formula/src/testFixtures/resources/Automotive02_V1/model.pdf";

        int exit_code = FeatJAR.runTest("formatConversion", "--input", pathToInputModel, "--output", pathToOutPutModel);
        assertEquals(1, exit_code);
    }

    /**
     * Tests whether information loss warnings are given when appropriate.
     */
    @Test
    void infoLossMapTest() {
        FormatConversion formatConversion = new FormatConversion();

        // output extension should not be found in information loss map
        assertEquals(2, formatConversion.infoLossMessage("xml", "pdf"));
        // this input / output file extension combination should trigger an info loss warning
        assertEquals(1, formatConversion.infoLossMessage("xml", "dot"));
        // this input / output file extension combination should NOT trigger an info loss warning
        assertEquals(0, formatConversion.infoLossMessage("xml", "xml"));
    }

    /**
     * Tests whether the converter can do an XML -> XML round trip with a basic feature model.
     */
    @Test
    void testWriteAndOverwrite() throws IOException {

        Path outputPath = Paths.get("../../Desktop/COPYTHIS.xml");
        FeatureModel model = generateMinimalModel();

        // let program write model to XML file
        new FormatConversion().saveFile(outputPath, model, "xml", true);

        // round trip: rebuild model from XML file
        FeatureModel retrievedModel =
                (FeatureModel) IO.load(outputPath, new XMLFeatureModelFormat()).get();

        assertEquals(model, retrievedModel);

        Files.deleteIfExists(outputPath);
    }
}
