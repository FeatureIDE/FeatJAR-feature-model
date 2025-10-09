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
package de.featjar.feature.model.io.yaml;

import de.featjar.base.data.Result;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.input.AInputMapper;
import de.featjar.base.tree.Trees;
import de.featjar.feature.model.analysis.AnalysisTree;
import de.featjar.feature.model.analysis.visitor.AnalysisTreeVisitor;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.yaml.snakeyaml.Yaml;

public class YAMLAnalysisFormat implements IFormat<AnalysisTree<?>> {

    @Override
    public String getName() {
        return "YAML";
    }

    @Override
    public String getFileExtension() {
        return "yaml";
    }

    @Override
    public boolean supportsParse() {
        return true;
    }

    @Override
    public boolean supportsWrite() {
        return true;
    }

    @Override
    public Result<String> serialize(AnalysisTree<?> analysisTree) {
    	Yaml yaml = new Yaml();
        return Result.of(yaml.dump(Trees.traverse(analysisTree, new AnalysisTreeVisitor()).get()));
    }

    @Override
    public Result<AnalysisTree<?>> parse(AInputMapper inputMapper) {
    	Yaml yaml = new Yaml();
    	HashMap<String, Object> yamlHashMap = (HashMap<String, Object>) yaml.load(inputMapper.get().text());
    	return Result.of(AnalysisTree.hashMapListYamlToTree(yamlHashMap));
    }
}
