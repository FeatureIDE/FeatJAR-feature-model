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

    enum FileTypes {
        XML,
        CSV,
        YAML,
        JSON
    }

    enum AnalysesScope {
        ALL,
        TREE_RELATED,
        CONSTRAINT_RELATED
    }

    public static final Option<FileTypes> FILE_TYPE =
            Option.newEnumOption("type", FileTypes.class).setDescription("Specifies file type");

    public static final Option<AnalysesScope> ANALYSES_SCOPE =
            Option.newEnumOption("scope", AnalysesScope.class).setDescription("Specifies scope of statistics");

    public static final Option<Boolean> PRETTY_PRINT =
            Option.newFlag("pretty").setDescription("Pretty prints the numbers");

    private HashMap<String, Integer> data;
    private FileTypes type;

    @Override
    public int run(OptionList optionParser) {

        // -----------------INPUT--------------------------------------------------

        // temporary dummy model
        // Path path = Paths.get("../formula/src/testFixtures/resources/Automotive02_V1/model.xml");

        Path path = optionParser.getResult(INPUT_OPTION).orElseThrow();
        Result<IFeatureModel> load = IO.load(path, FeatureModelFormats.getInstance());
        IFeatureModel model = load.orElseThrow();

        // -----------------COLLECTING STATS--------------------------------------

        data = collectStats(model);

        // -----------------WRITING TO FILE---------------------------------------

        if (optionParser.getResult(OUTPUT_OPTION).isPresent()) {
            Path outputPath = optionParser.getResult(OUTPUT_OPTION).get();
            // writeTo(outputPath);
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
    private void writeTo(Path path) {
        switch (type) {
            case XML:
                // TODO future Story Card: Write to XML
                // IO.save(new (), path, new XMLFeatureModelFormat());
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
        }
    }

    private HashMap<String, Integer> collectStats(IFeatureModel model) {

        HashMap<String, Integer> data = new HashMap<String, Integer>();

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
        StringBuilder outputString = new StringBuilder();

        for (Map.Entry<?, ?> entry : data.entrySet()) {
            outputString.append(String.format("%-30s : %s%n", entry.getKey(), entry.getValue()));
        }
        FeatJAR.log().message("STATISTICS ABOUT THE FEATURE MODEL:\n" + outputString);
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
