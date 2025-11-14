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
package de.featjar.feature.model.io.csv;

import de.featjar.base.data.Result;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.tree.Trees;
import de.featjar.feature.model.analysis.AnalysisTree;
import de.featjar.feature.model.analysis.visitor.AnalysisTreeVisitorCSV;
import java.util.ArrayList;
import java.util.Arrays;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvSchema;

/**
 * An IFormat class that take an AnalysisTree as input and can serialize it into CSV String.
 * With the CSV having only four columns (AnalysisType, Name, Value, Class)
 *
 * @author Mohammad Khair Almekkawi
 * @author Florian Beese
 */
public class CSVAnalysisFormat implements IFormat<AnalysisTree<?>> {

    @Override
    public String getName() {
        return "CSV";
    }

    @Override
    public String getFileExtension() {
        return "csv";
    }

    @Override
    public boolean supportsParse() {
        return false;
    }

    @Override
    public boolean supportsWrite() {
        return true;
    }

    @Override
    public Result<String> serialize(AnalysisTree<?> analysisTree) {
        CsvMapper CSVMapper = new CsvMapper();
        ArrayList<Object> nodesList = new ArrayList<Object>();
        nodesList.add(Arrays.asList("AnalysisType", "Name", "Class", "Value"));
        CsvSchema schema = CsvSchema.emptySchema()
                .withColumnSeparator(';')
                .withLineSeparator("\n")
                .withoutQuoteChar();
        nodesList.addAll(
                Trees.traverse(analysisTree, new AnalysisTreeVisitorCSV()).get());
        return Result.of(CSVMapper.writer(schema).writeValueAsString(nodesList));
    }
}
