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
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.base.io.IO;
import de.featjar.base.log.Log.Verbosity;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.io.xml.XMLFeatureModelFormat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

/**
 * @author Knut, Kilian & Benjamin
 */
public class FormatConversionTest {

    /**
     * {@return example feature model for testing purposes}
     */
    private FeatureModel generateModel() {
        FeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
        featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
        return featureModel;
    }

    private String inputPath = "../formula/src/testFixtures/resources/Automotive02_V1/model.xml";
    private String outputPath;


    /**
     * Attempts to write model to an incompatible file format (.pdf) and checks whether it's rejected correctly.
     *
     */
    @Test
    void fileWritingTest() throws IOException {

        outputPath = "model_fileWritingTest.xml";

        Files.deleteIfExists(Paths.get(outputPath));

        int exit_code = FeatJAR.runTest("formatConversion", "--input", inputPath, "--output", outputPath);
        assertEquals(0, exit_code);
        assertTrue(new File(outputPath).exists());

        Files.deleteIfExists(Paths.get(outputPath));
    }

    /**
     *
     *
     * Attempts to read model from an incompatible file format (.pdf) and checks whether it's rejected correctly.
     */
    @Test
    void invalidInput() throws IOException {

        inputPath = "../formula/src/testFixtures/resources/Automotive02_V1/model.pdf";
        outputPath = "model_invalidInput.xml";

        Files.deleteIfExists(Paths.get(outputPath));

        int exit_code = FeatJAR.runTest("formatConversion", "--input", inputPath, "--output", outputPath);
        assertEquals(1, exit_code);

        Files.deleteIfExists(Paths.get(outputPath));
    }

    /**
     * Attempts to write model to an incompatible file format (.pdf) and checks whether it's rejected correctly.
     */
    @Test
    void invalidOutput() throws IOException {

        outputPath = "model_invalidOutput.pdf";

        Files.deleteIfExists(Paths.get(outputPath));

        int exit_code = FeatJAR.runTest("formatConversion", "--input", inputPath, "--output", outputPath);
        assertEquals(2, exit_code);

        Files.deleteIfExists(Paths.get(outputPath));
    }

    /**
     *
     * checks if an info loss message is produced
     */
    @Test
    void infoLossMapTestTriggers() throws IOException {

        inputPath = "../formula/src/testFixtures/resources/Automotive02_V1/model.xml";
        outputPath = "model_invalidInput.dot";

        Files.deleteIfExists(Paths.get(outputPath));

        FeatJAR.runTest("formatConversion", "--input", inputPath, "--output", outputPath);


    	ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintStream stream = new PrintStream(out);
    	de.featjar.base.FeatJAR.Configuration config = FeatJAR.configure();
		config
    		.logConfig
    		.logToStream(stream, "", Verbosity.MESSAGE, Verbosity.WARNING);
    	FeatJAR.initialize(config);


        FormatConversion formatConversion = new FormatConversion();

        byte[] byteArray = out.toByteArray();
    	String string = new String(byteArray);
    	// System.out.println(string); // to check what was written to logger
    	String expected_output = "Info Loss:\n"
    			+ "	 Supports Feature attributes and metadata\n"
    			+ "   		xml: FULL\n"
    			+ "  		dot: NONE\n"
    			+ "	 Supports Mandatory and optional features\n"
    			+ "   		xml: FULL\n"
    			+ "  		dot: NONE\n";
    	assertTrue(string.contains(expected_output));

    	// assertEquals(expected_output, string);
    }

    /**
     * Tests whether the converter can do an XML -> XML round trip with a basic feature model.
     *
     */
    @Test
    void testWriteAndOverwrite() throws IOException {

        Path outputPath = Paths.get("model_testWriteAndOverwrite.xml");
        FeatureModel model = generateModel();

        Files.deleteIfExists(outputPath);

        // let program write model to XML file
        new FormatConversion().saveFile(outputPath, model, "xml", true);

        // round trip: rebuild model from XML file
        FeatureModel retrievedModel =
                (FeatureModel) IO.load(outputPath, new XMLFeatureModelFormat()).get();

        assertEquals(model, retrievedModel);

        Files.deleteIfExists(outputPath);
    }
}
