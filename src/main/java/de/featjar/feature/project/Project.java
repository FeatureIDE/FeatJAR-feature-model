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

    public Project(HashMap<String, Object> metaData, FeatureModel featureModel, List<ConfigurationPath> configurationPaths) {
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
