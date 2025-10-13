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
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.base.io.format.IFormat;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.io.FeatureModelFormats;
import de.featjar.formula.assignment.BooleanAssignmentValueMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Prints statistics about a provided Feature Model.
 *
 * @author Knut, Kilian & Benjamin
 */

// BooleanAssignmentValueMapFormat implements IFormat<BooleanAssignmentValueMap> 

public class ConfigurationFormatConversion implements ICommand {

    private static final List<String> supportedInputFileExtensions =
            FeatureModelFormats.getInstance().getExtensions().stream()
                    .filter(IFormat::supportsParse)
                    .map(IFormat::getFileExtension)
                    .collect(Collectors.toList());

    private static final List<String> supportedOutputFileExtensions =
            FeatureModelFormats.getInstance().getExtensions().stream()
                    .filter(IFormat::supportsWrite)
                    .map(IFormat::getFileExtension)
                    .collect(Collectors.toList());

    public static final Option<Path> INPUT_OPTION = Option.newOption("input", Option.PathParser)
            .setDescription("Path to input file. Accepted File Types: " + supportedInputFileExtensions)
            .setValidator(Option.PathValidator);

    public static final Option<Path> OUTPUT_OPTION = Option.newOption("output", Option.PathParser)
            .setDescription("Path to output file. Accepted File Types: " + supportedOutputFileExtensions);

    public static final Option<Boolean> OVERWRITE =
            Option.newFlag("overwrite").setDescription("Overwrite output file.");

    /**
     * @return all options registered for the calling class.
     */
    public final List<Option<?>> getOptions() {
        return Option.getAllOptions(getClass());
    }

    /**
     * For info loss map; indicates whether a feature is supported fully, partially, or not at all.
     */
    private enum SupportLevel {
        NO(0),
        YES(1);

        public final int rank;

        SupportLevel(int rank) {
            this.rank = rank;
        }

        boolean isLessThan(SupportLevel other) {
            return this.rank < other.rank;
        }
    }

    /**
     * For info loss map.
     * Saving name as well as a description in case we need to explain it to the user later.
     */
    private enum FileInfo {
        basicHierarchy("General hierarchical Structure"),
        subgroupHierarchy("Hierarchy with subgroups"),
        featureDescription("Features with descriptions"),
        featureAttributes("Features with attributes"),
        featureCardinality("Cardinality of features"),
        booleanOperators("Features of boolean operators"),
        allOperators("Features of all operators"),
        parseable("File can be used for input");

        public final String name;

        FileInfo(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * main function for handling format conversion
     * @param optionParser supplied by command line execution.
     *
     * @return 0 on success
     * 		   1 if output/input aren't present
     *         2 if input/output file type is invalid
     *         3 if the model could not be parsed,
     *         4 if a file is already present at output path and no overwrite is specified
     *         5 on IOException
     */
    @Override
    public int run(OptionList optionParser) {

        if (!checkIfInputOutputIsPresent(optionParser)) {
            return 1;
        }
        Path outputPath = optionParser.getResult(OUTPUT_OPTION).orElseThrow();

        // check if provided file extensions are supported
        String inputFileExtension =
                IO.getFileExtension(optionParser.getResult(INPUT_OPTION).get());
        String outputFileExtension =
                IO.getFileExtension(optionParser.getResult(OUTPUT_OPTION).get());
        if (!checkIfFileExtensionsValid(inputFileExtension, outputFileExtension)) {
            return 2;
        }

        // informing user about information loss during conversion between file formats
        infoLossMessage(inputFileExtension, outputFileExtension);

        // check if model was corrected extracted from input
        IFeatureModel model = inputParser(optionParser);
        if (model == null) {
            FeatJAR.log().error("No model parsed from input file!");
            return 3;
        }

        return saveFile(outputPath, model, outputFileExtension, optionParser.get(OVERWRITE));
    }

    /**
     * Informs user about potential information loss occurring during file conversion
     * @param inputFileExtension file extension of the input file (lower case, no leading dot)
     * @param outputFileExtension file extension of the output file (lower case, no leading dot)
     */
    private void infoLossMessage(String inputFileExtension, String outputFileExtension) {

        StringBuilder msg = new StringBuilder();
        msg.append("Info Loss:\n");

        Map<String, Map<FileInfo, SupportLevel>> infoLossMap = buildInfoLossMap();

        Map<FileInfo, SupportLevel> iSupports = infoLossMap.get(inputFileExtension);
        Map<FileInfo, SupportLevel> oSupports = infoLossMap.get(outputFileExtension);

        if (iSupports == null || oSupports == null) {
            return;
        }
        boolean infoLossPresent = false;
        for (FileInfo fileInfo : iSupports.keySet()) {
            SupportLevel iSupportLevel = iSupports.get(fileInfo);
            SupportLevel oSupportLevel = oSupports.get(fileInfo);

            if (oSupportLevel.isLessThan(iSupportLevel)) {
                if (!infoLossPresent) {
                    msg.append(String.format(
                            "%-46s  %s%n", "", inputFileExtension + " --> " + outputFileExtension + "\n"));
                    infoLossPresent = true;
                }

                msg.append(String.format("%-36s %14s  %5s%n", "    " + fileInfo, iSupportLevel, oSupportLevel));
            }
        }
        if (infoLossPresent) {
            FeatJAR.log().warning(msg.toString());
        } else {
            FeatJAR.log().info("No Information Loss from " + inputFileExtension + " to " + outputFileExtension + ".");
        }
    }

    /**
     *
     * {@return information loss map that tracks how well a file extension supports any given piece of information}
     */
    private Map<String, Map<FileInfo, SupportLevel>> buildInfoLossMap() {
        Map<String, Map<FileInfo, SupportLevel>> supportMap = new HashMap<>();

        buildInfoLossMapRegisterExt(
                "xml",
                Map.of(
                        FileInfo.basicHierarchy, SupportLevel.YES,
                        FileInfo.subgroupHierarchy, SupportLevel.NO,
                        FileInfo.featureDescription, SupportLevel.YES,
                        FileInfo.featureAttributes, SupportLevel.YES,
                        FileInfo.featureCardinality, SupportLevel.NO,
                        FileInfo.booleanOperators, SupportLevel.YES,
                        FileInfo.allOperators, SupportLevel.NO,
                        FileInfo.parseable, SupportLevel.YES),
                supportMap);

        buildInfoLossMapRegisterExt(
                "uvl",
                Map.of(
                        FileInfo.basicHierarchy, SupportLevel.YES,
                        FileInfo.subgroupHierarchy, SupportLevel.YES,
                        FileInfo.featureDescription, SupportLevel.YES,
                        FileInfo.featureAttributes, SupportLevel.YES,
                        FileInfo.featureCardinality, SupportLevel.YES,
                        FileInfo.booleanOperators, SupportLevel.YES,
                        FileInfo.allOperators, SupportLevel.YES,
                        FileInfo.parseable, SupportLevel.YES),
                supportMap);

        buildInfoLossMapRegisterExt(
                "dot",
                Map.of(
                        FileInfo.basicHierarchy, SupportLevel.YES,
                        FileInfo.subgroupHierarchy, SupportLevel.YES,
                        FileInfo.featureDescription, SupportLevel.YES,
                        FileInfo.featureAttributes, SupportLevel.YES,
                        FileInfo.featureCardinality, SupportLevel.YES,
                        FileInfo.booleanOperators, SupportLevel.YES,
                        FileInfo.allOperators, SupportLevel.YES,
                        FileInfo.parseable, SupportLevel.NO),
                supportMap);

        // if user forgot to set FileInfos: Support Level is automatically set to NONE
        for (String ext : supportMap.keySet()) {
            for (FileInfo fileInfo : FileInfo.values()) {
                supportMap.get(ext).putIfAbsent(fileInfo, SupportLevel.NO);
            }
        }

        return supportMap;
    }

    /**
     * Reinforces correct addition of infoLossMap entries
     * @param extension file extension that will be added
     * @param fileInfos pieces of file information as described in FileInfo enum
     * @param supportMap the information loss map that's being updated
     */
    private void buildInfoLossMapRegisterExt(
            String extension,
            Map<FileInfo, SupportLevel> fileInfos,
            Map<String, Map<FileInfo, SupportLevel>> supportMap) {
        if (fileInfos.size() != FileInfo.values().length) {
            FeatJAR.log()
                    .error("Info Loss Map: " + extension
                            + " was added with too many or too few FileInfos. Skipping this extension.");
            return;
        }
        supportMap.put(extension, new EnumMap<>(fileInfos));
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
     * Attempts to extract a feature model from the input file.
     * @param optionParser holds the command line parameters
     * @return Feature Model read out from input file. Will be null on failure.
     */
    private IFeatureModel inputParser(OptionList optionParser) {
        Path inputPath = optionParser.getResult(INPUT_OPTION).orElseThrow();
        IFeatureModel model = null;
        try {
            Result<IFeatureModel> load = IO.load(inputPath, FeatureModelFormats.getInstance());
            model = load.get();
        } catch (Exception e) {
            FeatJAR.log().error(e.getMessage());
        }
        return model;
    }

    /**
     * Saves the read feature model as the desired output file. Automatically fetches the appropriate format. Does error handling.
     * @param outputPath Full path to output file.
     * @param model Feature Model to be saved into the output file.
     * @param outputFileExtension extension of the output file. Used to fetch appropriate format.
     * @param overWriteOutputFile flag that decides whether existing output files with the same name should be overwritten.
     * @return 0 on success
     *         2 if an input/output file type is invalid
     *         4 if a file is already present at output path and no overwrite is specified
     *         5 on IOException
     */
    public int saveFile(Path outputPath, IFeatureModel model, String outputFileExtension, boolean overWriteOutputFile) {

        IFormat<IFeatureModel> format;

        Optional<IFormat<IFeatureModel>> outputFormats = FeatureModelFormats.getInstance().getExtensions().stream()
                .filter(IFormat::supportsWrite)
                .filter(formatTemp -> Objects.equals(outputFileExtension, formatTemp.getFileExtension()))
                .findFirst();
        if (outputFormats.isEmpty()) {
            FeatJAR.log().error("Unsupported output file extension: " + outputFileExtension);
            return 2;
        } else {
            format = outputFormats.get();
        }

        try {
            if (Files.exists(outputPath)) {
                if (overWriteOutputFile) {
                    FeatJAR.log().info("File already present at: " + outputPath + ". Continuing to overwrite File.");
                } else {
                    FeatJAR.log()
                            .error("Saving outputModel in File unsuccessful: File already present at: " + outputPath
                                    + ". To overwrite present file add --overwrite");
                    return 4;
                }
            }
            IO.save(model, outputPath, format);

        } catch (IOException e) {
            FeatJAR.log().error(e);
            return 5;
        }
        FeatJAR.log().message("Output model saved at: " + outputPath);
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
