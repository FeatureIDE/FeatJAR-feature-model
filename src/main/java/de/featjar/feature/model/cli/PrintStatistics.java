/*
 * Copyright (C) 2025 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula.
 *
 * formula is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula> for further information.
 */
package de.featjar.feature.model.cli;

import de.featjar.base.FeatJAR;
import de.featjar.base.cli.ACommand;
import de.featjar.base.cli.Option;
import de.featjar.base.cli.OptionList;
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.base.io.csv.CSVFile;
import de.featjar.base.tree.Trees;
import de.featjar.formula.cli.PrintCommand.WhitespaceString;
import de.featjar.formula.io.FormulaFormats;
import de.featjar.formula.io.textual.ExpressionSerializer;
import de.featjar.formula.io.textual.Symbols;
import de.featjar.formula.structure.IFormula;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;


/**
 * Prints statistics about a provided Feature Model.
 *
 * @author Knut & Kilian
 */
public class PrintStatistics extends ACommand {
	
    public static final Option<Boolean> PRETTY_PRINT = Option.newFlag("print")
            .setDescription("Pretty prints the numbers");
    
    public static final Option<Path> INPUT_OPTION = Option.newOption("path", Option.PathParser)
            .setDescription("Path to input file(s)");
    	 // .setValidator(Option.PathValidator)
    
	private HashMap<String, Integer> data = new HashMap<String, Integer>();
	
    @Override
    public int run(OptionList optionParser) {
    	Boolean bool_prettyPrint = optionParser.get(PRETTY_PRINT);
    	Path path = optionParser.get(INPUT_OPTION);
    	
    	
    	if (!path.equals(null)) {
        	FeatJAR.log().message(path);
    	}
    	if(bool_prettyPrint) {
    		FeatJAR.log().message("PRETTY");
    	}
    	
    	FeatJAR.log().message("STATISTICS ABOUT THE FEATURE MODEL:\n" + messageLog());
        
        return 0;
    }
    
    private StringBuilder messageLog() {
    	    	
        data.put("numOfTopFeatures", 3);
        data.put("numOfLeafFeatures", 12);
        data.put("treeDepth", 3);
        data.put("avgNumOfChildren", 3);
        data.put("numInOrGroups", 7);
        data.put("numInAltGroups", 5);
        data.put("numOfAtoms", 8);
        data.put("avgNumOfAtomsPerConstraints", 4);
        
        StringBuilder outputString = new StringBuilder();
        
        for (Map.Entry<?, ?> entry : data.entrySet()) {
        	outputString.append(String.format("%-30s : %s%n", entry.getKey(), entry.getValue()));
        }
    	return outputString;
    }
    
    private String writeTocsv() {
    	//Path path = new Path()
    	//CSVFile file = new CSVFile(path);
    	return "";
    }
    

    @Override
    public Optional<String> getDescription() {
        return Optional.of("Prints out statistics about a given Feature Model.");
    }

    @Override
    public Optional<String> getShortName() {
        return Optional.of("printStats");
    }
}
