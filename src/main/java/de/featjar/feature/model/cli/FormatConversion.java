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
import de.featjar.feature.model.analysis.*;
import de.featjar.feature.model.io.FeatureModelFormats;
import de.featjar.feature.model.io.xml.XMLFeatureModelFormat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

//--input "../formula/src/testFixtures/resources/Automotive02_V1/model.xml"  --scope all --pretty --output "c://home/deskop/model.xml"

/**
 * Prints statistics about a provided Feature Model.
 *
 * @author Knut, Kilian & Benjamin
 */
public class FormatConversion implements ICommand  {


	
	private static final List<String> supportedInputFileExtensions = Arrays.asList("csv", "xml", "yaml", "txt", "dot");
	private static final List<String> supportedOutputFileExtensions = Arrays.asList("csv", "xml", "yaml", "txt", "json");
	
	
	public static final Option<Path> INPUT_OPTION = Option.newOption("input", Option.PathParser)
            .setDescription("Path to input file. Accepted File Types: " + supportedInputFileExtensions)
            .setValidator(Option.PathValidator);

    public static final Option<Path> OUTPUT_OPTION = Option.newOption("output", Option.PathParser)
            .setDescription("Path to output file. Accepted File Types: " + supportedInputFileExtensions)
            .setValidator(Option.PathValidator);

    /**
     * {@return all options registered for the calling class}
     */
    public final List<Option<?>> getOptions() {
        return Option.getAllOptions(getClass());
    }


    /**
     * 
     * @param optionParser option parser supplied by command line execution
     *
     * @return 0 on success, 1 if in- or output paths are invalid, 2 on IOException, 3 if no model could be parsed from input file
     */
    @Override
    public int run(OptionList optionParser) {

        // check if there is a missing input / output argument
    	if(!checkIfInputOutputIsPresent(optionParser)) {
    		return 1;
    	}

        // check if provided file extensions are supported
        String inputFileExtension = IO.getFileExtension(optionParser.getResult(INPUT_OPTION).get());
        String outputFileExtension = IO.getFileExtension(optionParser.getResult(OUTPUT_OPTION).get());
        if (!checkIfFileExtensionsValid(inputFileExtension, outputFileExtension)) {
            return 1;
        };

        // check if model was corrected extracted from input
    	IFeatureModel model = inputParser(optionParser);
        if (model == null) {
            FeatJAR.log().error("No model parsed from input file!");
            return 3;
        }

        // check if output path is valid
        Path outputPath = optionParser.getResult(OUTPUT_OPTION).orElseThrow();
        if(!isValidOutputPath(outputPath)) {
            return 1;
        }

        // save file
        return saveFile(outputPath, model, outputFileExtension);
    }

    /**
     * Checks if input and output file extensions provided by user appear in list of supported extensions.
     * @param inputFileExtension: extension used for the input file
     * @param outputFileExtension extension used for the output file
     * @return true if both extensions are valid, false if either is invalid
     */
    private boolean checkIfFileExtensionsValid (String inputFileExtension, String outputFileExtension) {
        if (!supportedInputFileExtensions.contains(inputFileExtension)) {
            FeatJAR.log().error("Unsupported input file extension.");
            System.out.println("Received extension: " + inputFileExtension +
                    "\n Supported extensions: " + supportedInputFileExtensions);
            return false;
        }

        if (!supportedOutputFileExtensions.contains(outputFileExtension)) {
            FeatJAR.log().error("Unsupported output file extension.");
            System.out.println("Received extension: " + outputFileExtension +
                    "\n Supported extensions: " + supportedOutputFileExtensions);
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
	        return model;
	    }catch (Exception e) {
	    	FeatJAR.log().error(e.getMessage());
	    }
	    return model;
    }
    
    private boolean isValidOutputPath(Path outputPath) {
    	//
    	return true;
    }

    /**
     * Handles the saving of the output file. Chooses a method appropriate for the respective file type.
     * @param outputPath: full path to the output file
     * @param model: Feature model read from original input file
     * @param fileExtension IFormat that can write FeatureModels to our output file extension
     * @return 0 on success, 1 on invalid output file extension, 2 on IOException,
     */
    private int saveFile(Path outputPath, IFeatureModel model, String fileExtension) {
        IFormat<IFeatureModel> format;

        switch (fileExtension) {
            case "xml":
                format = new XMLFeatureModelFormat();
                break;
            default:
                // this still catches errors if the switch case construct has not implemented all supported file types!
                FeatJAR.log().error("Unsupported output file extension: " + fileExtension);
                return 1;
        }

        try {
            IO.save(model, outputPath, format);
        }catch (IOException e) {
            FeatJAR.log().error(e.getMessage());
            return 2;
        }
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
