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
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.analysis.AnalysisTree;
import de.featjar.feature.model.analysis.computation.ComputeDistributionFeatureSelections;
import de.featjar.feature.model.analysis.computation.ComputeFeatureCounter;
import de.featjar.feature.model.analysis.computation.ComputeNumberConfigurations;
import de.featjar.feature.model.analysis.computation.ComputeNumberVariables;
import de.featjar.feature.model.analysis.computation.ComputeUniformity;
import de.featjar.feature.model.io.FeatureModelFormats;
import de.featjar.feature.model.io.csv.CSVAnalysisFormat;
import de.featjar.feature.model.io.json.JSONAnalysisFormat;
import de.featjar.feature.model.io.transformer.AnalysisTreeTransformer;
import de.featjar.feature.model.io.yaml.YAMLAnalysisFormat;
import de.featjar.formula.assignment.BooleanAssignmentList;
import de.featjar.formula.io.BooleanAssignmentListFormats;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Prints statistics about given configuration(s)
 *
 * @author Mohammad Khair Almekkawi
 * @author Florian Beese
 */
public class PrintSampleStatistics extends ACommand {

    private int exitCode = 0;

    public static final Option<Path> INPUT_OPTION_FM = Option.newOption("inputFM", Option.PathParser)
            .setDescription("Path to input file containing feature model")
            .setValidator(Option.PathValidator);

    public static final Option<Path> INPUT_OPTION_CONFIGS = Option.newOption("inputConfig", Option.PathParser)
            .setDescription("Path to input file containing set of configuration(s)")
            .setValidator(Option.PathValidator);

    public static final Option<Boolean> PRETTY_PRINT =
            Option.newFlag("pretty").setDescription("Pretty prints the numbers");

    public static final Option<Boolean> UNPROCESSED =
            Option.newFlag("unprocessed").setDescription("Prints unprocessed data of uniformity statistics");

    @Override
    public int run(OptionList optionParser) {
        if (!optionParser.getResult(INPUT_OPTION_CONFIGS).isPresent()) {
            FeatJAR.log().error("No Input file containing config(s) attached");
            return 1;
        }

        if (!optionParser.getResult(INPUT_OPTION_FM).isPresent()) {
            FeatJAR.log().error("No Input file containing feature model attached");
            return 1;
        }

        Path pathConfig = optionParser.getResult(INPUT_OPTION_CONFIGS).orElseThrow();
        Result<BooleanAssignmentList> loadedConfig = IO.load(pathConfig, BooleanAssignmentListFormats.getInstance());
        Path pathFM = optionParser.getResult(INPUT_OPTION_FM).orElseThrow();
        Result<IFeatureModel> loadedFM = IO.load(pathFM, FeatureModelFormats.getInstance());

        LinkedHashMap<String, Object> data;
        IFeatureModel model = (IFeatureModel) loadedFM.orElseThrow();
        BooleanAssignmentList booleanAssignmentList = (BooleanAssignmentList) loadedConfig.orElseThrow();

        if (optionParser.get(UNPROCESSED)) {
            data = collectStats(booleanAssignmentList, model, true);
        } else {
            data = collectStats(booleanAssignmentList, model);
        }

        // if output path is specified, write statistics to file
        if (optionParser.getResult(OUTPUT_OPTION).isPresent()) {
            writeTo(optionParser.getResult(OUTPUT_OPTION).get(), data);
        }

        // printing statistics to console if no output file is specified
        if (optionParser.get(PRETTY_PRINT)) {
            printStatsPretty(data);
        } else if (!optionParser.getResult(OUTPUT_OPTION).isPresent()) {
            printStats(data);
        }

        return exitCode;
    }

    /**
     * writes statistics into a file, depending on file type
     * @param path: full path to output file
     * @param type: is extracted from provided output path, needs to be lower case
     */
    private void writeTo(Path path, LinkedHashMap<String, Object> data) {

        String type = IO.getFileExtension(path);
        Result<AnalysisTree<?>> tree = AnalysisTreeTransformer.hashMapToTree(data, "Analysis");

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
                    exitCode = 1;
                    break;
                default:
                    FeatJAR.log().error("File type not valid: " + type);
                    exitCode = 1;
            }
        } catch (Exception e) {
            FeatJAR.log().error(e);
        }
    }

    public LinkedHashMap<String, Object> collectStats(BooleanAssignmentList boolList, IFeatureModel model) {
        return collectStats(boolList, model, false);
    }

    /**
     * Gathers statistics about given configuration set
     *
     * @param boolList - configurations as BooleanAssignmentList
     * @param model - corresponding feature model
     * @return returns Map containing statistics
     */
    public LinkedHashMap<String, Object> collectStats(
            BooleanAssignmentList boolList, IFeatureModel model, boolean unprocessed) {

        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put(
                "Number of Configurations",
                Computations.of(boolList).map(ComputeNumberConfigurations::new).compute());
        data.put(
                "Number of Variables",
                Computations.of(boolList).map(ComputeNumberVariables::new).compute());
        data.put(
                "Distribution of feature selection",
                Computations.of(boolList)
                        .map(ComputeDistributionFeatureSelections::new)
                        .compute());
        data.put(
                "Feature counter",
                Computations.of(boolList).map(ComputeFeatureCounter::new).compute());
        if (unprocessed) {
            data.put(
                    "Uniformity",
                    Computations.of(model)
                            .map(ComputeUniformity::new)
                            .set(ComputeUniformity.BOOLEAN_ASSIGNMENT_LIST, boolList)
                            .set(ComputeUniformity.ANALYSIS, false)
                            .compute());
        } else {
            data.put(
                    "Uniformity",
                    Computations.of(model)
                            .map(ComputeUniformity::new)
                            .set(ComputeUniformity.BOOLEAN_ASSIGNMENT_LIST, boolList)
                            .compute());
        }

        return data;
    }
    /**
     * prints data in a pretty way
     *
     * @param data - data to be printed
     */
    public void printStatsPretty(LinkedHashMap<String, Object> data) {
        FeatJAR.log().message("STATISTICS ABOUT GIVEN SAMPLE:\n" + buildStringPrettyStats(data));
    }

    /**
     *
     * @param data Map of the gathered statistics. Keys name the respective stat, values save the stat value itself.
     * {@return StringBuilder with stats written into it in a pretty way}
     */
    public StringBuilder buildStringPrettyStats(LinkedHashMap<String, Object> data) {
        StringBuilder outputString = new StringBuilder();
        outputString.append(String.format("\n                %-40s  %n", "CONSTRAINT RELATED STATS\n"));
        for (Map.Entry<?, ?> entry : data.entrySet()) {
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
        FeatJAR.log().message("STATISTICS ABOUT GIVEN SAMPLE:\n" + data);
    }

    /**
     *
     * {@return brief description of this class}
     */
    @Override
    public Optional<String> getDescription() {
        return Optional.of("Prints out statistics about a given sample set.");
    }

    /**
     *
     * {@return short name of this class}
     */
    @Override
    public Optional<String> getShortName() {
        return Optional.of("printSampleStats");
    }
}
