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
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.analysis.*;
import de.featjar.feature.model.io.FeatureModelFormats;
import de.featjar.feature.model.io.xml.XMLFeatureModelFormat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Prints statistics about a provided Feature Model.
 *
 * @author Knut, Kilian & Benjamin
 */
public class FormatConversion implements ICommand  {


	
	private static final List<String> supportedInputFileExtensions = Arrays.asList("xml", "uvl", "dot");
	private static final List<String> supportedOutputFileExtensions = Arrays.asList("xml", "uvl", "dot");
	
	
	public static final Option<Path> INPUT_OPTION = Option.newOption("input", Option.PathParser)
            .setDescription("Path to input file. Accepted File Types: csv, xml, yaml, txt")
            .setValidator(Option.PathValidator);

	
	
	
    /**
     * Output option for saving files.
     */
    public static final Option<Path> OUTPUT_OPTION =
            Option.newOption("output", Option.PathParser).setDescription("Path to output file. Accepted File Types: csv, xml, yaml, txt, json");

    /**
     * {@return all options registered for the calling class}
     */
    public final List<Option<?>> getOptions() {
        return Option.getAllOptions(getClass());
    }
	
	//--input "../formula/src/testFixtures/resources/Automotive02_V1/model.xml"  --scope all --pretty --output "c://home/deskop/model.xml" 

    /**
     * 
     * @param optionParser the option parser
     *
     * @return returns 0 if successful, 1 in case of error
     */
    @Override
    public int run(OptionList optionParser) {
    	
    	// Valid formats: XML, UVL, GraphVis
    	
	
    	if(!checkIfInputOutputIsPresent(optionParser)) {
    		return 1;
    	}
    	IFeatureModel model = inputParser(optionParser); //model == null falls error occurred
    	
    	// check if provided file extensions are supported
	    String inputFileExtension = IO.getFileExtension(optionParser.getResult(INPUT_OPTION).get());
    	
	    if (!supportedInputFileExtensions.contains(inputFileExtension)) {
	    	System.out.println("supportedInputFileExtensions: " + supportedInputFileExtensions);
	    	System.out.println("input: " + IO.getFileExtension(optionParser.getResult(INPUT_OPTION).get()));
	    	FeatJAR.log().error("Unsupported input file extension.");
	    	return 2;
	    }
	    
	    String outputFileExtension = IO.getFileExtension(optionParser.getResult(OUTPUT_OPTION).get());
	    
	    if (!supportedOutputFileExtensions.contains(outputFileExtension)) {
	    	FeatJAR.log().error("Unsupported output file extension.");
	    	return 2;
	    }
	    
	    
	    if(saveFile(optionParser, model, outputFileExtension));
	    
    	
	    Path outputPath = optionParser.getResult(OUTPUT_OPTION).orElseThrow();
	    
	    if(!isValidOutputPath(outputPath)) {
	    	return 1;
	    }
	    
	   
	    	
        // saving file
        try {
            IO.save(model, outputPath, new XMLFeatureModelFormat());
            
        }catch (Exception e) {
        	FeatJAR.log().error(e.getMessage());
        }
        
        
        return 0;
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
    
    private boolean saveFile(OptionList optionParser, IFeatureModel model, String fileExtension) {
    	Path outputPath = optionParser.getResult(OUTPUT_OPTION).orElseThrow();
        try {
            IO.save(model, outputPath, new XMLFeatureModelFormat());
            return true;
        }catch (IOException e) {
        	FeatJAR.log().error(e.getMessage());
        }
	    return false;
    }
    

    /**
     *
     * {@return brief description of this class}
     */
    @Override
    public Optional<String> getDescription() {
        return Optional.of("Convert exisiting file of feature model into new format.");
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
