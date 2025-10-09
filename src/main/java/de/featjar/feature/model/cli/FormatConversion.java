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
import de.featjar.base.io.text.GenericTextFormat;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.io.FeatureModelFormats;
import de.featjar.feature.model.io.xml.GraphVizFeatureModelFormat;
import de.featjar.feature.model.io.xml.XMLFeatureModelFormat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

// formatConversion --input "../formula/src/testFixtures/resources/Automotive02_V1/model.xml" --output
// "../../Desktop/model.xml" --overwrite

// derzeit dynamisch implementiert von welchem dateityp man zu einem anderen dateityp konvertieren
// wir würden aber den daraus resultierenden information loss hard coden. Dynam

// Extensionpoints um auf UVL zuzugreifen

/**
 * Prints statistics about a provided Feature Model.
 *
 * @author Knut, Kilian & Benjamin
 */
public class FormatConversion implements ICommand {

    /*
    private static Map<String, List<String>> supportedFileExtensions;
       private static List<String> supportedInputFileExtensions;
       private static List<String> supportedOutputFileExtensions;
       */

    private static Map<String, List<String>> supportedFileExtensions = buildSupportedFileExtensions();
    private static List<String> supportedInputFileExtensions = supportedFileExtensions.get("input");
    private static List<String> supportedOutputFileExtensions = supportedFileExtensions.get("output");

    //    private static final List<String> supportedInputFileExtensions = Arrays.asList("csv", "xml", "yaml", "txt",
    // "dot", "uvl");
    //    private static final List<String> supportedOutputFileExtensions =
    //            Arrays.asList("csv", "xml", "yaml", "txt", "json", "dot", "uvl");

    public static final Option<Path> INPUT_OPTION = Option.newOption("input", Option.PathParser)
            .setDescription("Path to input file. Accepted File Types: " + supportedInputFileExtensions)
            .setValidator(Option.PathValidator);

    public static final Option<Path> OUTPUT_OPTION = Option.newOption("output", Option.PathParser)
            .setDescription("Path to output file. Accepted File Types: " + supportedInputFileExtensions);

    public static final Option<Boolean> OVERWRITE =
            Option.newFlag("overwrite").setDescription("Overwrite output file.");
    /**
     * {@return all options registered for the calling class}
     */
    public final List<Option<?>> getOptions() {
        return Option.getAllOptions(getClass());
    }

    // for info loss map
    private enum SupportLevel {
        NONE(0),
        PARTIAL(1),
        FULL(2);

        public final int rank;

        SupportLevel(int rank) {
            this.rank = rank;
        }

        boolean isLessThan(SupportLevel other) {
            return this.rank < other.rank;
        }
    }

    // for info loss map
    // saving name as well as a description in case we need to explain it to the user later
    private enum FileInfo {
        hierarchicalFeatureStructure("Hierarchical feature structure"),
        featureAttributesAndMetadata("Feature attributes and metadata"),
        mandatoryAndOptionalFeatures("Mandatory and optional features"),
        featureGroups(
                "Feature groups (AND, OR, XOR)",
                "AND groups are equivalent to cardinality groups ranging from 1 to 1, and OR from 1 to n.");

        public final String name;
        public final String description;

        FileInfo(String name) {
            this.name = name;
            this.description = "";
        }

        FileInfo(String name, String description) {
            this.name = name;
            this.description = description;
        }

        @Override
        public String toString() {
            return description.isEmpty() ? name : name + ": " + description;
        }
    }

    /**
     *
     * @param optionParser option parser supplied by command line execution
     *
     * @return 0 on success, 1 if in- or output paths are invalid, 2 on IOException, 3 if no model could be parsed from input file
     */
    @Override
    public int run(OptionList optionParser) {
        /*
        supportedFileExtensions = buildSupportedFileExtensions();
        supportedInputFileExtensions = supportedFileExtensions.get("input");
        supportedOutputFileExtensions = supportedFileExtensions.get("output");
        */

        if (!checkIfInputOutputIsPresent(optionParser)) {
            return 1;
        }

        // check if provided file extensions are supported
        String inputFileExtension =
                IO.getFileExtension(optionParser.getResult(INPUT_OPTION).get());
        String outputFileExtension =
                IO.getFileExtension(optionParser.getResult(OUTPUT_OPTION).get());
        if (!checkIfFileExtensionsValid(inputFileExtension, outputFileExtension)) {
            return 2;
        }

        infoLossMessage(inputFileExtension, outputFileExtension);

        // check if model was corrected extracted from input
        IFeatureModel model = inputParser(optionParser);
        if (model == null) {
            FeatJAR.log().error("No model parsed from input file!");
            return 3;
        }

        Path outputPath = optionParser.getResult(OUTPUT_OPTION).orElseThrow();

        return saveFile(outputPath, model, outputFileExtension, optionParser.get(OVERWRITE));
    }

    private static Map<String, List<String>> buildSupportedFileExtensions() {

        // todo can we do this cleaner?
        try {
            FeatJAR.initialize();
        } catch (Exception e) {
            System.out.println("Already Initialized, Caught");
        }

        List<IFormat<IFeatureModel>> supportedFileExtensions = null;

        try {
            supportedFileExtensions = FeatureModelFormats.getInstance().getExtensions();
        } catch (Exception e) {
            FeatJAR.log().error(e);
        }

        List<String> supportedInputFileExtensions = new ArrayList<>();
        List<String> supportedOutputFileExtensions = new ArrayList<>();

        for (IFormat<IFeatureModel> ext : supportedFileExtensions) {
            if (ext.supportsParse()) {
                supportedInputFileExtensions.add(ext.getFileExtension());
            }
            if (ext.supportsWrite()) {
                supportedOutputFileExtensions.add(ext.getFileExtension());
            }
        }

        return Map.of(
                "input", supportedInputFileExtensions,
                "output", supportedOutputFileExtensions);
    }

    // return 0 for no information loss. return 1 for information loss, return 2 on error due to unsupported input or
    // output file extensions
    public int infoLossMessage(String iExt, String oExt) {
        String msg = "Info Loss:\n";
        Map<String, Map<FileInfo, SupportLevel>> infoLossMap = buildInfoLossMap();

        System.out.print(supportedInputFileExtensions);
        System.out.print(supportedOutputFileExtensions);

        Map<FileInfo, SupportLevel> iSupports = infoLossMap.get(iExt);
        ;
        Map<FileInfo, SupportLevel> oSupports = infoLossMap.get(oExt);
        ;

        if (iSupports == null || oSupports == null) {
            return 2;
        }

        for (FileInfo fileInfo : iSupports.keySet()) {
            SupportLevel iSupportLevel = iSupports.get(fileInfo);
            SupportLevel oSupportLevel = oSupports.get(fileInfo);
            if (oSupportLevel.isLessThan(iSupportLevel)) {
                msg += "\t Supports " + fileInfo + "\n   \t\t" + iExt + ": " + iSupportLevel + "\n  \t\t" + oExt + ": "
                        + oSupportLevel + "\n";
            }
        }
        if (!msg.equals("Info Loss:\n")) {
            FeatJAR.log().warning(msg);
            return 1;
        } else {
            FeatJAR.log().message("No Information Loss from " + iExt + " to " + oExt + ".");
        }

        return 0;
    }

    private Map<String, Map<FileInfo, SupportLevel>> buildInfoLossMap() {
        Map<String, Map<FileInfo, SupportLevel>> supportMap = new HashMap<>();

        // set to eliminate duplicates
        Set<String> supportedFileExtensions = new LinkedHashSet<>(supportedInputFileExtensions);
        supportedFileExtensions.addAll(supportedOutputFileExtensions);

        // default values
        for (String fileExtension : supportedFileExtensions) {
            supportMap.put(fileExtension, new EnumMap<>(FileInfo.class)); // for each extension: add each feature
            for (FileInfo fileInfo : FileInfo.values()) {
                supportMap
                        .get(fileExtension)
                        .put(fileInfo, SupportLevel.NONE); // by default: all features are unsupported
            }
        }

        // fill with real values maybe dynamically?
        String extension = "xml";
        supportMap.get(extension).put(FileInfo.mandatoryAndOptionalFeatures, SupportLevel.FULL);
        supportMap.get(extension).put(FileInfo.featureAttributesAndMetadata, SupportLevel.FULL);
        supportMap.get(extension).put(FileInfo.hierarchicalFeatureStructure, SupportLevel.PARTIAL);
        supportMap.get(extension).put(FileInfo.featureGroups, SupportLevel.NONE);

        //        extension = "uvl";
        //        supportMap.get(extension).put(FileInfo.mandatoryAndOptionalFeatures, SupportLevel.NONE);
        //        supportMap.get(extension).put(FileInfo.featureAttributesAndMetadata, SupportLevel.NONE);
        //        supportMap.get(extension).put(FileInfo.hierarchicalFeatureStructure, SupportLevel.PARTIAL);
        //        supportMap.get(extension).put(FileInfo.featureGroups, SupportLevel.NONE);

        //        extension = "txt";
        //        supportMap.get(extension).put(FileInfo.mandatoryAndOptionalFeatures, SupportLevel.NONE);
        //        supportMap.get(extension).put(FileInfo.featureAttributesAndMetadata, SupportLevel.NONE);
        //        supportMap.get(extension).put(FileInfo.hierarchicalFeatureStructure, SupportLevel.PARTIAL);
        //        supportMap.get(extension).put(FileInfo.featureGroups, SupportLevel.NONE);

        return supportMap;
    }

    /**
     * Checks if input and output file extensions provided by user appear in list of supported extensions.
     * @param inputFileExtension: extension used for the input file
     * @param outputFileExtension extension used for the output file
     * @return true if both extensions are valid, false if either is invalid
     */
    private boolean checkIfFileExtensionsValid(String inputFileExtension, String outputFileExtension) {
        if (!supportedInputFileExtensions.contains(inputFileExtension)) {
            FeatJAR.log().error("Unsupported input file extension.");
            System.out.println("Received extension: " + inputFileExtension + "\n Supported extensions: "
                    + supportedInputFileExtensions);
            return false;
        }

        if (!supportedOutputFileExtensions.contains(outputFileExtension)) {
            FeatJAR.log().error("Unsupported output file extension.");
            System.out.println("Received extension: " + outputFileExtension + "\n Supported extensions: "
                    + supportedOutputFileExtensions);
            return false;
        }
        return true;
    }

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

    public int saveFile(Path outputPath, IFeatureModel model, String outputFileExtension, boolean overWriteOutputFile) {
        IFormat<IFeatureModel> format;
        switch (outputFileExtension) {
            case "xml":
                format = new XMLFeatureModelFormat();
                break;
            case "dot":
                format = new GraphVizFeatureModelFormat();
                break;
            case "txt":
                format = new GenericTextFormat<>();
                break;
                //            case "uvl":
                //            	TODO
            default:
                // this still catches errors if the switch case construct has not implemented all supported file types!
                FeatJAR.log().error("Unsupported output file extension: " + outputFileExtension);
                return 1;
        }
        try {
            if (Files.exists(outputPath)) {
                if (overWriteOutputFile) {
                    FeatJAR.log()
                            .message("File already present at: " + outputPath + "\n\tContinuing to overwrite File.");
                } else if (!overWriteOutputFile) {
                    FeatJAR.log()
                            .error("Saving outputModel in File unsuccessful: File already present at: " + outputPath
                                    + "\n\tTo overwrite present file add --overwrite");
                    return 1;
                }
            }
            IO.save(model, outputPath, format);

        } catch (IOException e) {
            FeatJAR.log().error(e);
            return 2;
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
        return Optional.of("Convert existing file of feature model into new format.");
    }

    /**
     *
     * {@return short name of this class}
     */
    @Override
    public Optional<String> getShortName() {
        return Optional.of("formatConversion");
    }
}
