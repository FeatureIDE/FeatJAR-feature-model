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
import de.featjar.base.io.IO;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.io.BooleanAssignmentListFormats;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

/**
 * @author Knut & Kilian
 */
public class ConfigurationFormatConversionTest {

    /**
     * Tests whether conversion from CSV to other formats produces files with the same content
     *
     */
    @Test
    void csvToOtherFormatsTest() throws IOException {

        String inputPath =
                "src/test/java/de/featjar/feature/model/cli/resources/BooleanAssignmentLists/BooleanAssignmentList.csv";

        String outputPath = "list_csvToDimacs.dimacs";
        int exit_code = FeatJAR.runTest(
                "configurationFormatConversion", "--input", inputPath, "--output", outputPath, "--overwrite");
        assertEquals(0, exit_code);
        assertTrue(new File(outputPath).exists());

        outputPath = "list_csvToBinary.bin";
        exit_code = FeatJAR.runTest(
                "configurationFormatConversion", "--input", inputPath, "--output", outputPath, "--overwrite");
        assertEquals(0, exit_code);
        assertTrue(new File(outputPath).exists());

        outputPath = "list_csvToList.list";
        exit_code = FeatJAR.runTest(
                "configurationFormatConversion",
                "--input",
                inputPath,
                "--output",
                outputPath,
                "--typeTXT",
                "default_txt",
                "--overwrite");
        assertEquals(0, exit_code);
        assertTrue(new File(outputPath).exists());
        Files.deleteIfExists(Paths.get(outputPath));

        outputPath = "list_csvToSimpleList.list";
        exit_code = FeatJAR.runTest(
                "configurationFormatConversion",
                "--input",
                inputPath,
                "--output",
                outputPath,
                "--typeTXT",
                "simple_txt",
                "--overwrite");
        assertEquals(0, exit_code);
        assertTrue(new File(outputPath).exists());
        Files.deleteIfExists(Paths.get(outputPath));

        // for additional testing the formats that can be parsed are compared to the original list
        FeatJAR.initialize();
        BooleanAssignmentList list_expected = IO.load(Paths.get(inputPath), BooleanAssignmentListFormats.getInstance())
                .get();
        BooleanAssignmentList list_csvToDimacs = IO.load(
                        Paths.get("list_csvToDimacs.dimacs"), BooleanAssignmentListFormats.getInstance())
                .get();
        BooleanAssignmentList list_csvToBinary = IO.load(
                        Paths.get("list_csvToBinary.bin"), BooleanAssignmentListFormats.getInstance())
                .get();
        FeatJAR.deinitialize();

        assertEquals(list_csvToDimacs.toString(), list_expected.toString());
        assertEquals(list_csvToBinary.toString(), list_expected.toString());
        Files.deleteIfExists(Paths.get("list_csvToDimacs.dimacs"));
        Files.deleteIfExists(Paths.get("list_csvToBinary.bin"));
    }

    /**
     * Tests whether conversion from DIMACS to other formats produces files with the same content
     *
     */
    @Test
    void dimacsToOtherFormatsTest() throws IOException {

        String inputPath =
                "src/test/java/de/featjar/feature/model/cli/resources/BooleanAssignmentLists/BooleanAssignmentList.dimacs";

        String outputPath = "list_dimacsToCSV.csv";
        int exit_code = FeatJAR.runTest(
                "configurationFormatConversion", "--input", inputPath, "--output", outputPath, "--overwrite");
        assertEquals(0, exit_code);
        assertTrue(new File(outputPath).exists());

        outputPath = "list_dimacsToBinary.bin";
        exit_code = FeatJAR.runTest(
                "configurationFormatConversion", "--input", inputPath, "--output", outputPath, "--overwrite");
        assertEquals(0, exit_code);
        assertTrue(new File(outputPath).exists());

        outputPath = "list_dimacsToList.list";
        exit_code = FeatJAR.runTest(
                "configurationFormatConversion",
                "--input",
                inputPath,
                "--output",
                outputPath,
                "--typeTXT",
                "default_txt",
                "--overwrite");
        assertEquals(0, exit_code);
        assertTrue(new File(outputPath).exists());
        Files.deleteIfExists(Paths.get(outputPath));

        outputPath = "list_dimacsToSimpleList.list";
        exit_code = FeatJAR.runTest(
                "configurationFormatConversion",
                "--input",
                inputPath,
                "--output",
                outputPath,
                "--typeTXT",
                "simple_txt",
                "--overwrite");
        assertEquals(0, exit_code);
        assertTrue(new File(outputPath).exists());
        Files.deleteIfExists(Paths.get(outputPath));

        // for additional testing the formats that can be parsed are compared to the original list
        FeatJAR.initialize();
        BooleanAssignmentList list_expected = IO.load(Paths.get(inputPath), BooleanAssignmentListFormats.getInstance())
                .get();
        BooleanAssignmentList list_dimacsToCSV = IO.load(
                        Paths.get("list_dimacsToCSV.csv"), BooleanAssignmentListFormats.getInstance())
                .get();
        BooleanAssignmentList list_dimacsToBinary = IO.load(
                        Paths.get("list_dimacsToBinary.bin"), BooleanAssignmentListFormats.getInstance())
                .get();
        FeatJAR.deinitialize();

        assertEquals(list_dimacsToCSV.toString(), list_expected.toString());
        assertEquals(list_dimacsToBinary.toString(), list_expected.toString());
        Files.deleteIfExists(Paths.get("list_dimacsToCSV.csv"));
        Files.deleteIfExists(Paths.get("list_dimacsToBinary.bin"));
    }

    /**
     * Tests whether conversion from binary to other formats produces files with the same content
     *
     */
    @Test
    void binaryToOtherFormatsTest() throws IOException {

        String inputPath =
                "src/test/java/de/featjar/feature/model/cli/resources/BooleanAssignmentLists/BooleanAssignmentList.bin";

        String outputPath = "list_binaryToCSV.csv";
        int exit_code = FeatJAR.runTest(
                "configurationFormatConversion", "--input", inputPath, "--output", outputPath, "--overwrite");
        assertEquals(0, exit_code);
        assertTrue(new File(outputPath).exists());

        outputPath = "list_binaryToDimacs.dimacs";
        exit_code = FeatJAR.runTest(
                "configurationFormatConversion", "--input", inputPath, "--output", outputPath, "--overwrite");
        assertEquals(0, exit_code);
        assertTrue(new File(outputPath).exists());

        outputPath = "list_binaryToList.list";
        exit_code = FeatJAR.runTest(
                "configurationFormatConversion",
                "--input",
                inputPath,
                "--output",
                outputPath,
                "--typeTXT",
                "default_txt",
                "--overwrite");
        assertEquals(0, exit_code);
        assertTrue(new File(outputPath).exists());
        Files.deleteIfExists(Paths.get(outputPath));

        outputPath = "list_binaryToSimpleList.list";
        exit_code = FeatJAR.runTest(
                "configurationFormatConversion",
                "--input",
                inputPath,
                "--output",
                outputPath,
                "--typeTXT",
                "simple_txt",
                "--overwrite");
        assertEquals(0, exit_code);
        assertTrue(new File(outputPath).exists());
        Files.deleteIfExists(Paths.get(outputPath));

        // for additional testing the formats that can be parsed are compared to the original list
        FeatJAR.initialize();
        BooleanAssignmentList list_expected = IO.load(Paths.get(inputPath), BooleanAssignmentListFormats.getInstance())
                .get();
        BooleanAssignmentList list_binaryToCSV = IO.load(
                        Paths.get("list_binaryToCSV.csv"), BooleanAssignmentListFormats.getInstance())
                .get();
        BooleanAssignmentList list_binaryToDimacs = IO.load(
                        Paths.get("list_binaryToDimacs.dimacs"), BooleanAssignmentListFormats.getInstance())
                .get();
        FeatJAR.deinitialize();

        assertEquals(list_binaryToCSV.toString(), list_expected.toString());
        assertEquals(list_binaryToDimacs.toString(), list_expected.toString());
        Files.deleteIfExists(Paths.get("list_binaryToCSV.csv"));
        Files.deleteIfExists(Paths.get("list_binaryToDimacs.dimacs"));
    }

    /**
     * Tests ...
     *
     */
    @Test
    void roundTripTest() throws IOException {

        String OriginalInputPath =
                "src/test/java/de/featjar/feature/model/cli/resources/BooleanAssignmentLists/BooleanAssignmentList.csv";

        // csv -> binary
        String outputPath = "list_csvToBinaryRoundTrip.bin";
        int exit_code = FeatJAR.runTest(
                "configurationFormatConversion",
                "--input",
                OriginalInputPath,
                "--output",
                outputPath,
                "--typeTXT",
                "simple_txt",
                "--overwrite");
        String inputPath = outputPath;
        assertEquals(0, exit_code);

        // binary -> dimacs
        outputPath = "list_binaryToDimacsRoundTrip.dimacs";
        exit_code = FeatJAR.runTest(
                "configurationFormatConversion",
                "--input",
                inputPath,
                "--output",
                outputPath,
                "--typeTXT",
                "simple_txt",
                "--overwrite");
        inputPath = outputPath;
        assertEquals(0, exit_code);

        // dimacs -> csv
        outputPath = "list_dimacsToCSVRoundTrip.csv";
        exit_code = FeatJAR.runTest(
                "configurationFormatConversion",
                "--input",
                inputPath,
                "--output",
                outputPath,
                "--typeTXT",
                "simple_txt",
                "--overwrite");
        assertEquals(0, exit_code);

        FeatJAR.initialize();
        BooleanAssignmentList list_expected = IO.load(
                        Paths.get(OriginalInputPath), BooleanAssignmentListFormats.getInstance())
                .get();
        BooleanAssignmentList list_final = IO.load(Paths.get(outputPath), BooleanAssignmentListFormats.getInstance())
                .get();
        assertEquals(list_final.toString(), list_expected.toString());
        FeatJAR.deinitialize();

        Files.deleteIfExists(Paths.get("list_csvToBinaryRoundTrip.bin"));
        Files.deleteIfExists(Paths.get("list_binaryToDimacsRoundTrip.dimacs"));
        Files.deleteIfExists(Paths.get("list_dimacsToCSVRoundTrip.csv"));
    }

    /**
     * Tests ...
     *
     */
    @Test
    void errorHandlingTest() throws IOException {

        String OriginalInputPath =
                "src/test/java/de/featjar/feature/model/cli/resources/BooleanAssignmentLists/BooleanAssignmentList.";
        String OriginalInputPathCSV = OriginalInputPath + "csv";
        String OriginalInputPathTXT = OriginalInputPath + "txt"; // invalid input path
        String OriginalInputPathBIN = OriginalInputPath + "bin";
        String OriginalInputPathXML = OriginalInputPath + "xml"; // invalid input path

        String outputPath = "list_csvToBinaryRoundTrip.";
        String outputPathCSV = outputPath + "csv";
        String outputPathBIN = outputPath + "bin";
        String outputPathXML = outputPath + "xml"; // invalid output path

        assertEquals(
                1,
                FeatJAR.runTest(
                        "configurationFormatConversion",
                        "--input",
                        "--output",
                        outputPathCSV,
                        "--typeTXT",
                        "simple_txt",
                        "--overwrite")); // missing input path
        assertEquals(
                1,
                FeatJAR.runTest(
                        "configurationFormatConversion",
                        "--input",
                        OriginalInputPathCSV,
                        "--output",
                        "--typeTXT",
                        "simple_txt",
                        "--overwrite")); // missing output path

        assertEquals(
                2,
                FeatJAR.runTest(
                        "configurationFormatConversion",
                        "--input",
                        OriginalInputPathXML,
                        "--output",
                        outputPathBIN,
                        "--typeTXT",
                        "simple_txt",
                        "--overwrite")); // xml is not a supported input type
        assertEquals(
                2,
                FeatJAR.runTest(
                        "configurationFormatConversion",
                        "--input",
                        OriginalInputPathTXT,
                        "--output",
                        outputPathBIN,
                        "--typeTXT",
                        "simple_txt",
                        "--overwrite")); // txt is not a supported input type
        assertEquals(
                2,
                FeatJAR.runTest(
                        "configurationFormatConversion",
                        "--input",
                        OriginalInputPathBIN,
                        "--output",
                        outputPathXML,
                        "--typeTXT",
                        "simple_txt",
                        "--overwrite")); // xml is not a supported output type

        assertEquals(
                0,
                FeatJAR.runTest(
                        "configurationFormatConversion",
                        "--input",
                        OriginalInputPathBIN,
                        "--output",
                        outputPathCSV,
                        "--typeTXT",
                        "simple_txt",
                        "--overwrite")); // creating file for next assertion
        assertEquals(
                3,
                FeatJAR.runTest(
                        "configurationFormatConversion",
                        "--input",
                        OriginalInputPathBIN,
                        "--output",
                        outputPathCSV,
                        "--typeTXT",
                        "simple_txt")); // failure to overwrite file because --overwrite is missing
    }
}
