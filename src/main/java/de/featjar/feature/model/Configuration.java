package de.featjar.feature.model;

import de.featjar.formula.assignment.Assignment;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of a {@link IConfiguration configuration}.
 *
 * @author Andreas Gerasimow
 */
public class Configuration implements IConfiguration.IMutableConfiguration {

    private final Map<String, ConfigurationEntry> configurationEntries;

    /**
     * Instantiates a configuration from a set of configuration entries.
     * @param configurationEntries Configuration entries to add.
     */
    public Configuration(Set<ConfigurationEntry> configurationEntries) {
        this.configurationEntries = new HashMap<>(
                configurationEntries.stream().collect(Collectors.toMap(ConfigurationEntry::getFeatureName, entry -> entry))
        );
    }

    /**
     * Instantiates a configuration from a feature model.
     * All values in the configuration entries will be <code>null</code>.
     * All features will be unselected in the configuration.
     * @param featureModel The feature model whose features are to be contained in the configuration.
     */
    public Configuration(IFeatureModel featureModel) {
        this.configurationEntries = new HashMap<>(
                featureModel.getFeatures().stream().collect(Collectors.toMap(
                        feature -> feature.getName().get(),
                        feature -> new ConfigurationEntry(feature.getName().get(), null, feature.getType(), ConfigurationEntry.SelectedStatus.UNSELECTED)))
        );
    }

    /**
     * Instantiates a configuration from an assignment.
     * All features will be unselected in the configuration.
     * @param assignment The assignment whose elements are to be contained in the configuration.
     */
    public Configuration(Assignment assignment) {
        this.configurationEntries = new HashMap<>();
        assignment.getAll().forEach((key, value) -> {
            configurationEntries.put(key, new ConfigurationEntry(
                    key,
                    value,
                    value.getClass(),
                    ConfigurationEntry.SelectedStatus.UNSELECTED
            ));
        });
    }

    /**
     * Instantiates an empty configuration.
     */
    public Configuration() {
        this.configurationEntries = new HashMap<>();
    }

    @Override
    public Set<ConfigurationEntry> getConfigurationEntries() {
        return new HashSet<>(configurationEntries.values());
    }

    @Override
    public Set<ConfigurationEntry> getUnselectedConfigurationEntries() {
        return configurationEntries
                .values()
                .stream()
                .filter((entry) -> entry.getSelectedStatus() == ConfigurationEntry.SelectedStatus.UNSELECTED)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ConfigurationEntry> getSelectedConfigurationEntries() {
        return configurationEntries
                .values()
                .stream()
                .filter((entry) ->
                            entry.getSelectedStatus() == ConfigurationEntry.SelectedStatus.MANUALLY_SELECTED ||
                            entry.getSelectedStatus() == ConfigurationEntry.SelectedStatus.AUTOMATICALLY_SELECTED)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ConfigurationEntry> getManuallySelectedConfigurationEntries() {
        return configurationEntries
                .values()
                .stream()
                .filter((entry) -> entry.getSelectedStatus() == ConfigurationEntry.SelectedStatus.MANUALLY_SELECTED)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ConfigurationEntry> getAutomaticallySelectedConfigurationEntries() {
        return configurationEntries
                .values()
                .stream()
                .filter((entry) -> entry.getSelectedStatus() == ConfigurationEntry.SelectedStatus.AUTOMATICALLY_SELECTED)
                .collect(Collectors.toSet());
    }

    // modifier methods
    @Override
    public ConfigurationEntry addEntry(ConfigurationEntry entry) {
        configurationEntries.put(entry.getFeatureName(), entry);
        return entry;
    }

    @Override
    public boolean removeEntry(ConfigurationEntry entry) {
        return configurationEntries.remove(entry.getFeatureName(), entry);
    }

    @Override
    public boolean removeEntry(String featureName) {
        return configurationEntries.remove(featureName) != null;
    }

    // other methods

    @Override
    public Assignment toAssignment() {
        LinkedHashMap<String, Object> variableValuePairs = new LinkedHashMap<>();
        for (ConfigurationEntry entry : configurationEntries.values()) {
            variableValuePairs.put(entry.getFeatureName(), entry.getValue());
        }
        return new Assignment(variableValuePairs);
    }

    @Override
    public boolean fitsToFeatureModel(IFeatureModel featureModel) {
        if (featureModel.getFeatures().size() != configurationEntries.size()) {
            return false;
        }

        return featureModel.getFeatures().stream().allMatch((feature) -> {
            ConfigurationEntry entry = configurationEntries.get(feature.getName().get());
            return entry != null && entry.getFeatureName().equals(feature.getName().get()) && entry.getType().equals(feature.getType());
        });
    }

    // hashCode / toString / equals

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Configuration that = (Configuration) o;
        return Objects.equals(configurationEntries, that.configurationEntries);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(configurationEntries);
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "configurationEntries=" + configurationEntries +
                '}';
    }
}
