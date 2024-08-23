package de.featjar.feature.model;

import java.util.Objects;

/**
 * An entry for a {@link Configuration configuration}.
 *
 * @author Andreas Gerasimow
 */
public class ConfigurationEntry {
    public enum SelectedStatus {
        UNSELECTED("unselected"),
        MANUALLY_SELECTED("manually"),
        AUTOMATICALLY_SELECTED("automatic");

        private final String name;

        SelectedStatus(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private String featureName;
    private Object value;
    private Class<?> type;
    private SelectedStatus selectedStatus;

    public ConfigurationEntry(String featureName, Object value, Class<?> type, SelectedStatus selectedStatus) {
        setFeatureName(featureName);
        setValue(value);
        setType(type);
        setSelectedStatus(selectedStatus);
    }

    public String getFeatureName() {
        return featureName;
    }

    public Object getValue() {
        return value;
    }

    public Class<?> getType() {
        return type;
    }

    public SelectedStatus getSelectedStatus() {
        return selectedStatus;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public void setSelectedStatus(SelectedStatus selectedStatus) {
        this.selectedStatus = selectedStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigurationEntry that = (ConfigurationEntry) o;
        return Objects.equals(featureName, that.featureName) && Objects.equals(value, that.value) && Objects.equals(type, that.type) && selectedStatus == that.selectedStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(featureName, value, type, selectedStatus);
    }

    @Override
    public String toString() {
        return "ConfigurationEntry{" +
                "featureName='" + featureName + '\'' +
                ", value=" + value +
                ", type=" + type +
                ", selectedStatus=" + selectedStatus +
                '}';
    }
}
