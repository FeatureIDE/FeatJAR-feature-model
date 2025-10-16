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
import de.featjar.feature.model.analysis.AnalysisTree;
import de.featjar.feature.model.analysis.ComputeFeatureAverageNumberOfChildren;
import de.featjar.feature.model.analysis.ComputeFeatureFeaturesCounter;
import de.featjar.feature.model.analysis.ComputeFeatureGroupDistribution;
import de.featjar.feature.model.analysis.ComputeFeatureTopFeatures;
import de.featjar.feature.model.analysis.ComputeFeatureTreeDepth;
import de.featjar.feature.model.computation.ComputeAtomsCount;
import de.featjar.feature.model.computation.ComputeAverageConstraint;
import de.featjar.feature.model.computation.ComputeFeatureDensity;
import de.featjar.feature.model.computation.ComputeOperatorDistribution;
import de.featjar.feature.model.io.FeatureModelFormats;
import de.featjar.feature.model.analysis.visualization.*;
import de.featjar.feature.model.io.csv.CSVAnalysisFormat;
import de.featjar.feature.model.io.json.JSONAnalysisFormat;
import de.featjar.feature.model.io.transformer.AnalysisTreeTransformer;
import de.featjar.feature.model.io.yaml.YAMLAnalysisFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
public class PrintStatistics extends ACommand {

    public enum AnalysesScope {
        ALL,
        TREE_RELATED,
        CONSTRAINT_RELATED
    }
    public enum Visualize {
        OPTION1,
        OPTION2,
        OPTION3
    }

    private int exit_status = 0;

    public static final Option<AnalysesScope> ANALYSES_SCOPE =
            Option.newEnumOption("scope", AnalysesScope.class).setDescription("Specifies scope of statistics");

    public static final Option<Boolean> PRETTY_PRINT =
            Option.newFlag("pretty").setDescription("Pretty prints the numbers");

    public static final Option<Boolean> OVERWRITE =
            Option.newFlag("overwrite").setDescription("Overwrite output file.");
    
    public static final Option<Path> OUTPUT_OPTION_VISUALIZE =
            Option.newOption("path_visualize", Option.PathParser).setDescription("Path to save visualization as pdf.");

    /**
     * main method for gathering, printing and writing statistics of a feature model
     * @param optionParser the option parser
     *
     * @return returns 0 if successful, 1 in case of error
     */
    @Override
    public int run(OptionList optionParser) {

        // checking if input model has been specified
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

        // printing pretty if PRETTY flag specified, printing only compact if there is no output specified
        if (optionParser.get(PRETTY_PRINT)) {
            printStatsPretty(data);
        } else if (!optionParser.getResult(OUTPUT_OPTION).isPresent()) {
            printStats(data);
        }

        // if output path is specified, write statistics to file
        if (optionParser.getResult(OUTPUT_OPTION).isPresent()) {

            Path outputPath = optionParser.get(OUTPUT_OPTION);

            if (Files.exists(outputPath)) {
                if (optionParser.get(OVERWRITE)) {
                    FeatJAR.log().info("File already present at: " + outputPath + ". Continuing to overwrite File.");
                } else {
                    FeatJAR.log()
                            .error("Saving outputModel in File unsuccessful: File already present at: " + outputPath
                                    + ".\nTo overwrite present file add --overwrite");
                    return 1;
                }
            }
            writeTo(outputPath, data);
            FeatJAR.log().message("Feature Model saved at: " + outputPath);
        }
        
        if(optionParser.getResult(OUTPUT_OPTION_VISUALIZE).isPresent() ) {
        	//TODO aufrufen der Funktionen von Benjamin und Valentin aus VisualizeFeatureModelStats
            AnalysisTree<?> tree = AnalysisTreeTransformer.hashMapToTree(data, IO.getFileExtension(path)).get();
            VisualizeGroupDistribution vizGroup;

            vizGroup = new VisualizeGroupDistribution(tree);
            vizGroup.exportChartToPDF(0, optionParser.get(OUTPUT_OPTION).toString());

        }
        
        return exit_status;
    }

    /**
     * writes statistics into a file, depending on file type
     * @param path: full path to output file. can be "csv", "yaml" or "json"
     * @param data: expects data about a feature model as LinkedHashMap<Strong, Object> but will be converted to Result<AnalysisTree<?>>
     * @throws IOException
     */
    private void writeTo(Path path, LinkedHashMap<String, Object> data) {
        String type = IO.getFileExtension(path);
        Result<AnalysisTree<?>> tree = AnalysisTreeTransformer.hashMapToTree(data, type);

        try {
            switch (type) {
                case "csv":
                    IO.save(tree.get(), path, new CSVAnalysisFormat());
                    break;
                case "yaml":
                    IO.save(tree.get(), path, new YAMLAnalysisFormat());
                    break;
                case "json":
                    IO.save(tree.get(), path, new JSONAnalysisFormat());
                    break;
                case "":
                    FeatJAR.log().error("Output file does not include file type.");
                    exit_status = 1;
                    break;
                default:
                    FeatJAR.log().error("File type not valid: " + type);
                    exit_status = 1;
            }
        } catch (Exception e) {
            FeatJAR.log().error(e);
        }
    }

    /**
     * method for collecting statistics of the provided feature model depending on specified scope of information (all, constraint related, tree related)
     * @param model: a feature model from which statistics will be collected
     * @param scope: describes whether only constraint-related, only tree-related, or both kinds of statistics are to be collected
     * @return LinkedHashMap with statistics data, keys are descriptive strings, values types depend on statistic (Integer, Float, HashMap)
     */
    public LinkedHashMap<String, Object> collectStats(FeatureModel model, AnalysesScope scope) {

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();

        if (scope == AnalysesScope.ALL || scope == AnalysesScope.CONSTRAINT_RELATED) {

            // fetching constraint related statistics
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

            if (computational_opDensity.size() != 0) {
                data.put("Operator Distribution", computational_opDensity);
            }
        }

        if ((scope == AnalysesScope.ALL || scope == AnalysesScope.TREE_RELATED)) {

            // fetching tree related statistics
            List<IFeatureTree> trees = model.getRoots();
            String treePrefix;

            for (int i = 0; i < trees.size(); i++) {
                treePrefix = "[Tree " + (i + 1) + "] ";
                IFeatureTree tree = trees.get(i);
                data.put(
                        treePrefix + "Average Number of Children",
                        Computations.of(tree)
                                .map(ComputeFeatureAverageNumberOfChildren::new)
                                .compute());
                data.put(
                        treePrefix + "Number of Top Features",
                        Computations.of(tree)
                                .map(ComputeFeatureTopFeatures::new)
                                .compute());
                data.put(
                        treePrefix + "Number of Leaf Features",
                        Computations.of(tree)
                                .map(ComputeFeatureFeaturesCounter::new)
                                .compute());
                data.put(
                        treePrefix + "Tree Depth",
                        Computations.of(tree).map(ComputeFeatureTreeDepth::new).compute());
                data.put(
                        treePrefix + "Group Distribution",
                        Computations.of(tree)
                                .map(ComputeFeatureGroupDistribution::new)
                                .compute());
            }
        }

        return data;
    }

    /**
     *
     * @param data Map of the gathered statistics. Keys name the respective stat, values save the stat value itself.
     */
    public void printStatsPretty(LinkedHashMap<String, Object> data) {
        FeatJAR.log().message("STATISTICS ABOUT THE FEATURE MODEL:\n" + buildStringPrettyStats(data));
    }

    /**
     *
     * @param data Map of the gathered statistics. Keys name the respective stat, values save the stat value itself.
     * {@return StringBuilder with stats written into it in a pretty way}
     */
    public StringBuilder buildStringPrettyStats(LinkedHashMap<String, Object> data) {
        StringBuilder outputString = new StringBuilder();

        for (Map.Entry<?, ?> entry : data.entrySet()) {
            if (entry.getKey().equals("Number of Atoms")) {
                outputString.append(String.format("\n                %-40s  %n", "CONSTRAINT RELATED STATS\n"));
            } else if (entry.getKey().equals("[Tree 1] Average Number of Children")) {
                outputString.append(String.format("\n                %-40s  %n", "TREE RELATED STATS\n"));
            }
            if (entry.getValue() instanceof Map) {
                Map<?, ?> nestedMap = (Map<?, ?>) entry.getValue();
                outputString.append(String.format("%-40s%n", entry.getKey()));
                for (Map.Entry<?, ?> nestedEntry : nestedMap.entrySet()) {
                    outputString.append(String.format(
                            "%-40s : %s%n", "           " + nestedEntry.getKey(), nestedEntry.getValue()));
                }
            } else {
                outputString.append(String.format("%-40s : %s%n", entry.getKey(), entry.getValue()));
            }
        }
        return outputString;
    }

    /**
     * Prints gathered statistics in a compact format.
     * @param data: the previously computed data packaged line by line: String names the stat, Object holds the data.
     */
    public void printStats(LinkedHashMap<String, Object> data) {
        FeatJAR.log().message("STATISTICS ABOUT THE FEATURE MODEL:\n" + data);
    }

    /**
     *
     * {@return brief description of this class}
     */
    @Override
    public Optional<String> getDescription() {
        return Optional.of("Prints out statistics about a given Feature Model.");
    }

    /**
     *
     * {@return short name of this class}
     */
    @Override
    public Optional<String> getShortName() {
        return Optional.of("printStats");
    }
}
