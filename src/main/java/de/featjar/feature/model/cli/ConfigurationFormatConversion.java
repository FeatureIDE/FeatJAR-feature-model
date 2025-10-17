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

import de.featjar.base.FeatJAR;
import de.featjar.base.cli.ICommand;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.io.IO;
import de.featjar.base.io.format.IFormat;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.io.BooleanAssignmentListFormats;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Prints statistics about a provided Feature Model.
 *
 * @author Knut & Kilian
 */
public class ConfigurationFormatConversion implements ICommand {

    public enum TypeTXT {
        SIMPLE_TXT("simple_text"),
        DEFAULT_TXT("text");

        public final String description;

        TypeTXT(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    private static final List<String> supportedInputFileExtensions =
            BooleanAssignmentListFormats.getInstance().getExtensions().stream()
                    .filter(IFormat::supportsParse)
                    .map(IFormat::getFileExtension)
                    .collect(Collectors.toList());

    private static final List<String> supportedOutputFileExtensions =
            BooleanAssignmentListFormats.getInstance().getExtensions().stream()
                    .filter(IFormat::supportsWrite)
                    .map(IFormat::getFileExtension)
                    .collect(Collectors.toList());

    private static final List<String> supportedOutputFileNames =
            BooleanAssignmentListFormats.getInstance().getExtensions().stream()
                    .filter(IFormat::supportsWrite)
                    .map(f -> f.getName().toLowerCase())
                    .collect(Collectors.toList());

    private static final List<String> supportedOutputFileNamesWithExtensions =
            BooleanAssignmentListFormats.getInstance().getExtensions().stream()
                    .filter(IFormat::supportsWrite)
                    .map(f -> f.getName().toLowerCase() + " (." + f.getFileExtension() + ")")
                    .collect(Collectors.toList());

    public static final Option<Path> INPUT_OPTION = Option.newOption("input", Option.PathParser)
            .setDescription("Path to input file. Accepted File Types: " + supportedInputFileExtensions);

    public static final Option<Path> OUTPUT_OPTION = Option.newOption("output", Option.PathParser)
            .setDescription("Path to output file. Accepted File Types: " + supportedOutputFileNamesWithExtensions);

    public static final Option<Boolean> OVERWRITE =
            Option.newFlag("overwrite").setDescription("Overwrite existing file at output path.");

    public static final Option<String> FORMAT_TYPE = Option.newStringEnumOption(
                    "format", supportedOutputFileNames.toArray(new String[0]))
            .setDescription("Format can be specified. Necessary if output path has .list extension.");

    /**
     * @return all options registered for the calling class.
     */
    public final List<Option<?>> getOptions() {
        return Option.getAllOptions(getClass());
    }

    /**
     * main function for handling conversion of BooleanAssignmentList files.
     * @param optionParser supplied by command line execution.
     *
     * @return 0 on success
     * 		   1 on invalid input or output path
     * 		   2 on unsupported input or output file extension
     * 	 	   3 on failure to save BooleanAssignmentList because file already exists on path directory and --overwrite flag is not used
     * 		   4 on conflicting .list extension in outputPath and no existing --format to specify the .list type
     */
    @Override
    public int run(OptionList optionParser) {

        if (!checkIfInputOutputIsPresent(optionParser)) {
            return 1;
        }
        ;

        String format_type = "";
        if (optionParser.getResult(FORMAT_TYPE).isPresent()) {
            format_type = optionParser.getResult(FORMAT_TYPE).get();

        } else if (optionParser.getResult(OUTPUT_OPTION).get().toString().endsWith(".list")) {

            String outputBooleanAssignmentListFormat =
                    BooleanAssignmentListFormats.getInstance().getExtensions().stream()
                            .filter(IFormat::supportsWrite)
                            .map(f -> f.getName().toLowerCase())
                            .filter(f -> f.endsWith("list"))
                            .collect(Collectors.joining("', '"));

            FeatJAR.log()
                    .error("Specify format for .list file using the --format command. Possible options: '"
                            + outputBooleanAssignmentListFormat + "'.");
            return 4;
        }

        String intputFileExtension =
                IO.getFileExtension(optionParser.getResult(INPUT_OPTION).get().toString());
        String outputFileExtension =
                IO.getFileExtension(optionParser.getResult(OUTPUT_OPTION).get().toString());

        if (!checkIfFileExtensionsValid(intputFileExtension, outputFileExtension)) {
            return 2;
        }

        if (format_type.endsWith("list")) {
            FeatJAR.log().warning("No parser exists for '.list' files.");
        }

        // loading list from input path
        BooleanAssignmentList list = IO.load(
                        optionParser.getResult(INPUT_OPTION).orElseThrow(), BooleanAssignmentListFormats.getInstance())
                .get();

        // writing data to file
        return saveFile(
                optionParser.getResult(OUTPUT_OPTION).orElseThrow(), list, format_type, optionParser.get(OVERWRITE));
    }

    /**
     * Checks if input and output file extensions provided by user appear in list of supported extensions.
     * @param inputFileExtension: extension used for the input file
     * @param outputFileExtension extension used for the output file
     * @return true if both extensions are valid, false if either is invalid
     */
    private boolean checkIfFileExtensionsValid(String inputFileExtension, String outputFileExtension) {
        if (!supportedInputFileExtensions.contains(inputFileExtension)) {
            FeatJAR.log()
                    .error("Unsupported input file extension.\n"
                            + "Received extension: " + inputFileExtension + "\nSupported extensions: "
                            + supportedInputFileExtensions);
            return false;
        }

        if (!supportedOutputFileExtensions.contains(outputFileExtension)) {
            FeatJAR.log()
                    .error("Unsupported output file extension.\n"
                            + "Received extension: " + outputFileExtension + "\nSupported extensions: "
                            + supportedOutputFileExtensions);
            return false;
        }
        return true;
    }

    /**
     *
     * @param optionParser holds the command line parameters
     * {@return true if an input and output path were provided, otherwise false}
     */
    private boolean checkIfInputOutputIsPresent(OptionList optionParser) {
        if (!optionParser.getResult(INPUT_OPTION).isPresent()) {
            FeatJAR.log().error("No input path provided.");
            return false;
        } else if (!optionParser.getResult(OUTPUT_OPTION).isPresent()) {
            FeatJAR.log().error("No output path provided.");
            return false;
        }
        return true;
    }

    /**
     * Saves the opened BooleanAssignmentList as a different desired BooleanAssignmentList file. Automatically detects the appropriate format. Does error handling.
     * @param outputPath Full path to output file including extension.
     * @param inputList BooleanAssignmentList to be saved into the output file.
     * @param formatType String that can specify format (csv, binary, dimacs, literallist, variablelist)
     * @param overwriteDemanded flag that decides whether existing output file with the same name should be overwritten.
     * @return 0 on success
     *
     */
    public int saveFile(
            Path outputPath, BooleanAssignmentList inputList, String formatType, boolean overwriteDemanded) {

        Optional<IFormat<BooleanAssignmentList>> outputBooleanAssignmentListFormat = null;

        // if --format specifies the format
        if (supportedOutputFileNames.contains(formatType)) {

            outputBooleanAssignmentListFormat = BooleanAssignmentListFormats.getInstance().getExtensions().stream()
                    .filter(IFormat::supportsWrite)
                    .filter(f -> f.getName().toLowerCase().equals(formatType.toLowerCase()))
                    .findFirst();

            // user can specify different formats by using both --format and a path with another file extension. In that
            // case a warning is printed
            if (!IO.getFileExtension(outputPath)
                    .equals(outputBooleanAssignmentListFormat.get().getFileExtension())) {
                FeatJAR.log()
                        .warning("Writing using the "
                                + outputBooleanAssignmentListFormat.get().getName() + " format into a ."
                                + IO.getFileExtension(outputPath) + " file.");
            }

            // automatically extracting format from output path
        } else {
            outputBooleanAssignmentListFormat = BooleanAssignmentListFormats.getInstance().getExtensions().stream()
                    .filter(IFormat::supportsWrite)
                    .filter(formatTemp ->
                            Objects.equals(IO.getFileExtension(outputPath), formatTemp.getFileExtension()))
                    .findFirst();
        }

        try {
            if (Files.exists(outputPath)) {
                if (overwriteDemanded) {
                    FeatJAR.log().info("File already present at: " + outputPath + ". Continuing to overwrite File.");
                } else {
                    FeatJAR.log()
                            .error("Saving list in File unsuccessful: File already present at: " + outputPath
                                    + ". To overwrite present file add --overwrite");
                    return 3;
                }
            }
            IO.save(inputList, outputPath, outputBooleanAssignmentListFormat.get());
            FeatJAR.log().message("Output list successfully saved at: " + outputPath);

        } catch (Exception e) {
            FeatJAR.log().error(e);
        }

        return 0;
    }

    /**
     *
     * {@return brief description of this class}
     */
    @Override
    public Optional<String> getDescription() {
        return Optional.of("Convert configuration Format into new configuration format.");
    }

    /**
     *
     * {@return short name of this class}
     */
    @Override
    public Optional<String> getShortName() {
        return Optional.of("configurationFormatConversion");
    }
}
