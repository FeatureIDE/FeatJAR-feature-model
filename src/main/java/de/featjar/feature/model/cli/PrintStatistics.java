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
import java.io.IOException;
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

    enum FileTypes {
        XML,
        CSV,
        YAML,
        JSON,
        TXT
    }

    enum AnalysesScope {
        ALL,
        TREE_RELATED,
        CONSTRAINT_RELATED
    }

    // options as command line arguments
    public static final Option<FileTypes> FILE_TYPE =
            Option.newEnumOption("type", FileTypes.class).setDescription("Specifies file type");

    public static final Option<AnalysesScope> ANALYSES_SCOPE =
            Option.newEnumOption("scope", AnalysesScope.class).setDescription("Specifies scope of statistics");

    public static final Option<Boolean> PRETTY_PRINT =
            Option.newFlag("pretty").setDescription("Pretty prints the numbers");

    private HashMap<String, Integer> data;

    @Override
    public int run(OptionList optionParser) {

        // -----------------INPUT--------------------------------------------------

        Path path = optionParser.getResult(INPUT_OPTION).orElseThrow();
        Result<IFeatureModel> load = IO.load(path, FeatureModelFormats.getInstance());
        IFeatureModel model = load.orElseThrow();

        // -----------------COLLECTING STATS--------------------------------------

        if (optionParser.getResult(ANALYSES_SCOPE).isPresent()) {
            data = collectStats(model, optionParser.get(ANALYSES_SCOPE));
        } else {
            data = collectStats(model, AnalysesScope.ALL);
        }

        // -----------------WRITING TO FILE---------------------------------------

        // output path & file type specified
        if (optionParser.getResult(OUTPUT_OPTION).isPresent()
                && optionParser.getResult(FILE_TYPE).isPresent()) {

            try {
                writeTo(
                        optionParser.getResult(OUTPUT_OPTION).get(),
                        optionParser.getResult(FILE_TYPE).get());
            } catch (IOException e) {
                FeatJAR.log().error(e);
            }

            // output path specified, but no file type
        } else if (optionParser.getResult(OUTPUT_OPTION).isPresent()) {

            FeatJAR.log().warning("Output path provided, but no file type specified.");
        }

        // ----------------PRINTING IN CONSOLE------------------------------------

        if (optionParser.get(PRETTY_PRINT)) {
            printStatsPretty();
        } else {
            printStats();
        }

        return 0;
    }

    // temporary for format type output
    private void writeTo(Path path, FileTypes type) throws IOException {
        switch (type) {
            case XML:
                // TODO future Story Card: Write to XML
                // Example: IO.save(new (), path, new XMLFeatureModelFormat());
                break;
            case CSV:
                // TODO future Story Card: Write to CSV
                break;
            case YAML:
                // TODO future Story Card: Write to YAML
                break;
            case JSON:
                // TODO future Story Card: Write to JSON
                break;
            case TXT:
                // TODO future Story Card: Write to TXT
        }
    }

    private HashMap<String, Integer> collectStats(IFeatureModel model, AnalysesScope scope) {

        HashMap<String, Integer> data = new HashMap<String, Integer>();

        if (scope == AnalysesScope.ALL || scope == AnalysesScope.CONSTRAINT_RELATED) {

            // For Example model.getConstraintInfo()

        } else if ((scope == AnalysesScope.ALL || scope == AnalysesScope.TREE_RELATED)) {

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
