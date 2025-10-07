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
import de.featjar.base.cli.ACommand;
import de.featjar.base.cli.ICommand;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.computation.Computations;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.analysis.*;
import de.featjar.feature.model.computation.ComputeAtomsCount;
import de.featjar.feature.model.computation.ComputeAverageConstraint;
import de.featjar.feature.model.computation.ComputeFeatureDensity;
import de.featjar.feature.model.computation.ComputeOperatorDistribution;
import de.featjar.feature.model.io.FeatureModelFormats;
import de.featjar.feature.model.io.xml.GraphVizFeatureModelFormat;
import de.featjar.feature.model.io.xml.XMLFeatureModelFormat;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Prints statistics about a provided Feature Model.
 *
 * @author Knut, Kilian & Benjamin
 */
public class FormatConversion implements ICommand  {
	
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
    	
    	// opening file
    	Path inputPath = optionParser.getResult(INPUT_OPTION).orElseThrow();
        Result<IFeatureModel> load = IO.load(inputPath, FeatureModelFormats.getInstance());
        FeatureModel model = (FeatureModel) load.orElseThrow();
    	
        // saving file
        Path outputPath = optionParser.getResult(OUTPUT_OPTION).orElseThrow();
        
        Path test = Paths.get("");
        System.out.println(test.toAbsolutePath());
        
        Path target = Paths.get(test.toAbsolutePath()+"model.dot");
        
        
        try {
        	
            IO.save(model, target, new XMLFeatureModelFormat());
        	FeatJAR.log().message("HALLO");
            
        }catch (Exception e) {
        	FeatJAR.log().error(e.getMessage());
        }
        
        
        return 0;
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
