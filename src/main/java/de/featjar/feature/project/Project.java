/*
 * Copyright (C) 2024 FeatJAR-Development-Team
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
package de.featjar.feature.project;

import de.featjar.feature.model.FeatureModel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Defines a feature model project.
 *
 * @author Andreas Gerasimow
 */
public class Project {

    public static class ConfigurationPath {

        private final Object configuration;
        private final Path path;

        public ConfigurationPath(Object configuration, Path path) {
            this.configuration = configuration;
            this.path = path;
        }

        public Object getConfiguration() {
            return this.configuration;
        }

        public Path getPath() {
            return this.path;
        }
    }

    private final HashMap<String, Object> metaData;
    private final FeatureModel featureModel;
    private final List<ConfigurationPath> configurationPaths;

    public Project(FeatureModel featureModel) {
        this.metaData = new HashMap<>();
        this.featureModel = featureModel;
        this.configurationPaths = new ArrayList<>();
    }

    public Project(
            HashMap<String, Object> metaData, FeatureModel featureModel, List<ConfigurationPath> configurationPaths) {
        this.metaData = metaData;
        this.featureModel = featureModel;
        this.configurationPaths = configurationPaths;
    }

    public HashMap<String, Object> getMetaData() {
        return this.metaData;
    }

    public FeatureModel getFeatureModel() {
        return this.featureModel;
    }

    public List<ConfigurationPath> getConfigurationPaths() {
        return this.configurationPaths;
    }
}
