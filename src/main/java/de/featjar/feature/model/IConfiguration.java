package de.featjar.feature.model;

import de.featjar.formula.assignment.Assignment;

import java.util.Set;

/**
 * Indicates which features are selected in a {@link FeatureModel feature model}.
 *
 * @author Andreas Gerasimow
 */
public interface IConfiguration {

    /**
     * @return All configuration entries, regardless of the selected status.
     */
    Set<ConfigurationEntry> getConfigurationEntries();

    /**
     * @return All unselected configuration entries.
     */
    Set<ConfigurationEntry> getUnselectedConfigurationEntries();

    /**
     * @return All selected configuration entries (manually and automatically).
     */
    Set<ConfigurationEntry> getSelectedConfigurationEntries();

    /**
     * @return All manually selected configuration entries.
     */
    Set<ConfigurationEntry> getManuallySelectedConfigurationEntries();

    /**
     * @return All automatically selected configuration entries.
     */
    Set<ConfigurationEntry> getAutomaticallySelectedConfigurationEntries();

    /**
     * @return Converts the configuration to an {@link Assignment assignment}.
     */
    Assignment toAssignment();

    /**
     * @return <code>true</code> if configuration fits to {@link FeatureModel feature model}, else <code>false</code>
     */
    boolean fitsToFeatureModel(IFeatureModel featureModel);

    default IMutableConfiguration mutate() {
        return (IMutableConfiguration) this;
    }

    interface IMutableConfiguration extends IConfiguration {
        ConfigurationEntry addEntry(ConfigurationEntry entry);

        boolean removeEntry(ConfigurationEntry entry);

        boolean removeEntry(String featureName);
    }
}
