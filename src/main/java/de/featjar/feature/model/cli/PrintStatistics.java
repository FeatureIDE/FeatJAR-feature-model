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
import de.featjar.base.data.Result;
import de.featjar.base.io.IO;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.io.FeatureModelFormats;
import java.nio.file.Path;
import java.util.HashMap;
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

    private HashMap<String, Integer> data;

    @Override
    public int run(OptionList optionParser) {

        if (!optionParser.getResult(INPUT_OPTION).isPresent()) {
            FeatJAR.log().error("No Input file attached");
            return 1;
        }

        // opening input model
        Path path = optionParser.getResult(INPUT_OPTION).orElseThrow();
        Result<IFeatureModel> load = IO.load(path, FeatureModelFormats.getInstance());
        IFeatureModel model = load.orElseThrow();

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
            printStatsPretty();
        } else {
            printStats();
        }

        return exit_status;
    }

    private void writeTo(Path path, String type) {

        switch (type) {
            case "xml":
                // TODO future Story Card: Write to XML
                // IO.save(new Object(data), path, new XMLFeatureModelFormat());
                // IO.sa
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

    private HashMap<String, Integer> collectStats(IFeatureModel model, AnalysesScope scope) {

        HashMap<String, Integer> data = new HashMap<String, Integer>();

        if (scope == AnalysesScope.ALL || scope == AnalysesScope.CONSTRAINT_RELATED) {

            // For Example data.put(model.getConstraintInfo())

        }

        if ((scope == AnalysesScope.ALL || scope == AnalysesScope.TREE_RELATED)) {

            // For Example model.getTreeDepth()

        }

        // dummy values, will be handled by functions of other teams
        data.put("numOfTopFeatures", 3);
        data.put("numOfLeafFeatures", 12);
        data.put("treeDepth", 3);
        data.put("avgNumOfChildren", 3);
        data.put("numInOrGroups", 7);
        data.put("numInAltGroups", 5);
        data.put("numOfAtoms", 8);
        data.put("avgNumOfAtomsPerConstraints", 4);

        return data;
    }

    public void printStatsPretty() {
        FeatJAR.log().message("STATISTICS ABOUT THE FEATURE MODEL:\n" + buildPrettyStats());
    }

    private StringBuilder buildPrettyStats() {
        StringBuilder outputString = new StringBuilder();

        for (Map.Entry<?, ?> entry : data.entrySet()) {
            outputString.append(String.format("%-30s : %s%n", entry.getKey(), entry.getValue()));
        }
        return outputString;
    }

    public void printStats() {
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
