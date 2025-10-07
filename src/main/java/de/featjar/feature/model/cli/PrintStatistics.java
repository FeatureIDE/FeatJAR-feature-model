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
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Prints statistics about a provided Feature Model.
 *
 * @author Knut & Kilian
 */
public class PrintStatistics extends ACommand {

    enum AnalysesScope {
        ALL,
        TREE_RELATED,
        CONSTRAINT_RELATED
    }

    private int exit_status = 0;

    // options as command line arguments
    public static final Option<AnalysesScope> ANALYSES_SCOPE =
            Option.newEnumOption("scope", AnalysesScope.class).setDescription("Specifies scope of statistics");

    public static final Option<Boolean> PRETTY_PRINT =
            Option.newFlag("pretty").setDescription("Pretty prints the numbers");

    @Override
    public int run(OptionList optionParser) {

        if (!optionParser.getResult(INPUT_OPTION).isPresent()) {
            FeatJAR.log().error("No Input file attached");
            return 1;
        }

        // opening input model
        Path path = optionParser.getResult(INPUT_OPTION).orElseThrow();
        Result<IFeatureModel> load = IO.load(path, FeatureModelFormats.getInstance());
        LinkedHashMap<String, Object> data;

        FeatureModel model = (FeatureModel) load.orElseThrow();

        // collecting statistics of the model, checking if scope is specified
        if (optionParser.getResult(ANALYSES_SCOPE).isPresent()) {
            data = collectStats(model, optionParser.get(ANALYSES_SCOPE));
        } else {
        	data = collectStats(model, AnalysesScope.ALL);
        }

        // if output path is specified, write statistics to file
        if (optionParser.getResult(OUTPUT_OPTION).isPresent()) {
            Path outputPath = optionParser.get(OUTPUT_OPTION);
            String fileExtension = IO.getFileExtension(outputPath);
            writeTo(optionParser.getResult(OUTPUT_OPTION).get(), fileExtension);
        }

        // printing statistics to console
        if (optionParser.get(PRETTY_PRINT)) {
            printStatsPretty(data);
        } else {
            printStats(data);
        }

        return exit_status;
    }

    private void writeTo(Path path, String type) {

        switch (type) {
            case "xml":
                // TODO future Story Card: Write to XML
                // IO.save(new Object(data), path, new XMLFeatureModelFormat());
                break;
            case "csv":
                // TODO future Story Card: Write to CSV
                break;
            case "yaml":
                // TODO future Story Card: Write to YAML
                break;
            case "json":
                // TODO future Story Card: Write to JSON
                break;
            case "txt":
                // TODO future Story Card: Write to TXT
                break;
            case "":
                FeatJAR.log().error("Output file does not include file type.");
                exit_status = 1;
                break;
            default:
                FeatJAR.log().error("File type not valid: " + type);
                exit_status = 1;
        }
    }

    public LinkedHashMap<String, Object> collectStats(FeatureModel model, AnalysesScope scope) {

    	LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();

        if (scope == AnalysesScope.ALL || scope == AnalysesScope.CONSTRAINT_RELATED) {

            // Fetching constraint related statistics
            data.put(
                    "Number of Atoms",
                    Computations.of(model).map(ComputeAtomsCount::new).compute());
            data.put(
                    "Feature Density",
                    Computations.of(model).map(ComputeFeatureDensity::new).compute());
            data.put(
                    "Average Constraints",
                    Computations.of(model).map(ComputeAverageConstraint::new).compute());

            HashMap<String, Integer> computational_opDensity =
                    Computations.of(model).map(ComputeOperatorDistribution::new).compute();

            data.put("Operator Distribution", computational_opDensity);
        }

        if ((scope == AnalysesScope.ALL || scope == AnalysesScope.TREE_RELATED)) {


            // Fetching tree related statistics

            List<IFeatureTree> trees = model.getRoots();
            String treePrefix;

            for (int i = 0; i < trees.size(); i++) {
                treePrefix = "[Tree " + (i + 1) + "] ";

                IFeatureTree tree = trees.get(i);

                // avg num of children
                data.put(
                        treePrefix + "Average Number of Childen",
                        Computations.of(tree)
                                .map(ComputeFeatureAverageNumberOfChildren::new)
                                .compute());

                // num of top features
                data.put(
                        treePrefix + "Number of Top Features",
                        Computations.of(tree)
                                .map(ComputeFeatureTopFeatures::new)
                                .compute());

                // num of leaf features
                data.put(
                        treePrefix + "Number of Leaf Features",
                        Computations.of(tree)
                                .map(ComputeFeatureFeaturesCounter::new)
                                .compute());

                // tree depth
                data.put(
                        treePrefix + "Tree Depth",
                        Computations.of(tree).map(ComputeFeatureTreeDepth::new).compute());

                // group distribution
                data.put(
                        treePrefix + "Group Distribution",
                        Computations.of(tree)
                                .map(ComputeFeatureGroupDistribution::new)
                                .compute());
            }
        }

        return data;
    }

    public void printStatsPretty(LinkedHashMap<String, Object> data) {
        FeatJAR.log().message("STATISTICS ABOUT THE FEATURE MODEL:\n" + buildStringPrettyStats(data));
    }

    public StringBuilder buildStringPrettyStats(LinkedHashMap<String, Object> data) {
        StringBuilder outputString = new StringBuilder();
        
       
        
        for (Map.Entry<?, ?> entry : data.entrySet()) {
        	
        	if(entry.getKey().equals("Number of Atoms")) {
        		outputString.append(String.format("\n\t\t%-40s  %n", "CONSTRAINT RELATED STATS\n"));
        	
        	} else if(entry.getKey().equals("[Tree 1] Average Number of Childen")) {
        		outputString.append(String.format("\n\t\t%-40s  %n", "TREE RELATED STATS\n"));

        	}
            if (entry.getValue() instanceof Map) {
                Map<?, ?> nestedMap = (Map<?, ?>) entry.getValue();

                outputString.append(String.format("%-40s%n", entry.getKey()));

                for (Map.Entry<?, ?> nestedEntry : nestedMap.entrySet()) {
                    outputString.append(
                            String.format("%-33s : %s%n", "\t   " + nestedEntry.getKey(), nestedEntry.getValue()));
                }
            } else {
                outputString.append(String.format("%-40s : %s%n", entry.getKey(), entry.getValue()));
            }
        }
        return outputString;
    }

    public void printStats(LinkedHashMap<String, Object> data) {
        FeatJAR.log().message("STATISTICS ABOUT THE FEATURE MODEL:\n" + data);
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
