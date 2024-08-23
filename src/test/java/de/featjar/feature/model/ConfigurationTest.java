package de.featjar.feature.model;

import de.featjar.formula.assignment.Assignment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Set;

public class ConfigurationTest {

    private static Set<ConfigurationEntry> standardConfigEntries;

    @BeforeAll
    public static void setUpClass() {
        standardConfigEntries = Set.of(
                new ConfigurationEntry("f1", 1, Integer.class, ConfigurationEntry.SelectedStatus.UNSELECTED),
                new ConfigurationEntry("f2", 2, Integer.class, ConfigurationEntry.SelectedStatus.UNSELECTED),
                new ConfigurationEntry("f3", "a", String.class, ConfigurationEntry.SelectedStatus.MANUALLY_SELECTED),
                new ConfigurationEntry("f4", "b", String.class, ConfigurationEntry.SelectedStatus.MANUALLY_SELECTED),
                new ConfigurationEntry("f5", 3f, Float.class, ConfigurationEntry.SelectedStatus.AUTOMATICALLY_SELECTED),
                new ConfigurationEntry("f6", 4f, Float.class, ConfigurationEntry.SelectedStatus.AUTOMATICALLY_SELECTED)
        );
    }

    @Test
    public void testConfigurationConstructorList() {
        IConfiguration config = new Configuration(standardConfigEntries);
        Assertions.assertEquals(standardConfigEntries, config.getConfigurationEntries());
    }

    @Test
    public void testConfigurationConstructorFeatureModel() {
        IFeatureModel fm = new FeatureModel();
        IFeature f1 = fm.mutate().addFeature("f1");
        f1.mutate().setType(Integer.class);
        IFeature f2 = fm.mutate().addFeature("f2");
        f2.mutate().setType(String.class);
        IFeature f3 = fm.mutate().addFeature("f3");
        f3.mutate().setType(Float.class);

        IConfiguration config = new Configuration(fm);

        Set<ConfigurationEntry> configEntries = Set.of(
                new ConfigurationEntry("f1", null, Integer.class, ConfigurationEntry.SelectedStatus.UNSELECTED),
                new ConfigurationEntry("f2", null, String.class, ConfigurationEntry.SelectedStatus.UNSELECTED),
                new ConfigurationEntry("f3", null, Float.class, ConfigurationEntry.SelectedStatus.UNSELECTED)
        );

        Assertions.assertEquals(configEntries, config.getConfigurationEntries());
    }

    @Test
    public void testConfigurationConstructorAssignment() {
        LinkedHashMap<String, Object> variableValuePairs = new LinkedHashMap<>();
        variableValuePairs.put("f1", 1);
        variableValuePairs.put("f2", "a");
        variableValuePairs.put("f3", 3f);
        Assignment a = new Assignment(variableValuePairs);

        IConfiguration config = new Configuration(a);

        Set<ConfigurationEntry> configEntries = Set.of(
                new ConfigurationEntry("f1", 1, Integer.class, ConfigurationEntry.SelectedStatus.UNSELECTED),
                new ConfigurationEntry("f2", "a", String.class, ConfigurationEntry.SelectedStatus.UNSELECTED),
                new ConfigurationEntry("f3", 3f, Float.class, ConfigurationEntry.SelectedStatus.UNSELECTED)
        );

        Assertions.assertEquals(configEntries, config.getConfigurationEntries());
    }

    @Test
    public void testGetUnselectedConfigurationEntries() {
        IConfiguration config = new Configuration(standardConfigEntries);

        Set<ConfigurationEntry> configEntries = Set.of(
                new ConfigurationEntry("f1", 1, Integer.class, ConfigurationEntry.SelectedStatus.UNSELECTED),
                new ConfigurationEntry("f2", 2, Integer.class, ConfigurationEntry.SelectedStatus.UNSELECTED)
        );

        Assertions.assertEquals(configEntries, config.getUnselectedConfigurationEntries());
    }

    @Test
    public void testGetSelectedConfigurationEntries() {
        IConfiguration config = new Configuration(standardConfigEntries);

        Set<ConfigurationEntry> configEntries = Set.of(
                new ConfigurationEntry("f3", "a", String.class, ConfigurationEntry.SelectedStatus.MANUALLY_SELECTED),
                new ConfigurationEntry("f4", "b", String.class, ConfigurationEntry.SelectedStatus.MANUALLY_SELECTED),
                new ConfigurationEntry("f5", 3f, Float.class, ConfigurationEntry.SelectedStatus.AUTOMATICALLY_SELECTED),
                new ConfigurationEntry("f6", 4f, Float.class, ConfigurationEntry.SelectedStatus.AUTOMATICALLY_SELECTED)
        );

        Assertions.assertEquals(configEntries, config.getSelectedConfigurationEntries());
    }

    @Test
    public void testGetManuallySelectedConfigurationEntries() {
        IConfiguration config = new Configuration(standardConfigEntries);

        Set<ConfigurationEntry> configEntries = Set.of(
                new ConfigurationEntry("f3", "a", String.class, ConfigurationEntry.SelectedStatus.MANUALLY_SELECTED),
                new ConfigurationEntry("f4", "b", String.class, ConfigurationEntry.SelectedStatus.MANUALLY_SELECTED)
        );

        Assertions.assertEquals(configEntries, config.getManuallySelectedConfigurationEntries());
    }

    @Test
    public void testGetAutomaticallySelectedConfigurationEntries() {
        IConfiguration config = new Configuration(standardConfigEntries);

        Set<ConfigurationEntry> configEntries = Set.of(
                new ConfigurationEntry("f5", 3f, Float.class, ConfigurationEntry.SelectedStatus.AUTOMATICALLY_SELECTED),
                new ConfigurationEntry("f6", 4f, Float.class, ConfigurationEntry.SelectedStatus.AUTOMATICALLY_SELECTED)
        );

        Assertions.assertEquals(configEntries, config.getAutomaticallySelectedConfigurationEntries());
    }

    @Test
    public void testModifications() {
        IConfiguration config = new Configuration(Set.of(
                new ConfigurationEntry("f2", 2, Integer.class, ConfigurationEntry.SelectedStatus.UNSELECTED),
                new ConfigurationEntry("f3", "a", String.class, ConfigurationEntry.SelectedStatus.MANUALLY_SELECTED),
                new ConfigurationEntry("f4", "b", String.class, ConfigurationEntry.SelectedStatus.MANUALLY_SELECTED),
                new ConfigurationEntry("f5", 3f, Float.class, ConfigurationEntry.SelectedStatus.AUTOMATICALLY_SELECTED),
                new ConfigurationEntry("f6", 4f, Float.class, ConfigurationEntry.SelectedStatus.AUTOMATICALLY_SELECTED),
                new ConfigurationEntry("f7", 4f, Float.class, ConfigurationEntry.SelectedStatus.AUTOMATICALLY_SELECTED),
                new ConfigurationEntry("f8", 4f, Float.class, ConfigurationEntry.SelectedStatus.AUTOMATICALLY_SELECTED)
        ));

        config.mutate().addEntry(new ConfigurationEntry("f1", 1, Integer.class, ConfigurationEntry.SelectedStatus.UNSELECTED));
        config.mutate().removeEntry("f7");
        config.mutate().removeEntry(new ConfigurationEntry("f8", 4f, Float.class, ConfigurationEntry.SelectedStatus.AUTOMATICALLY_SELECTED));

        Assertions.assertEquals(standardConfigEntries, config.getConfigurationEntries());
    }

    @Test
    public void testToAssignment() {
        IConfiguration config = new Configuration(standardConfigEntries);
        Assignment a = new Assignment("f1", 1, "f2", 2, "f3", "a", "f4", "b", "f5", 3f, "f6", 4f);

        Assertions.assertEquals(a, config.toAssignment());
    }

    @Test
    public void testFitsToFeatureModel() {
        IFeatureModel fm = new FeatureModel();
        IFeature f1 = fm.mutate().addFeature("f1");
        f1.mutate().setType(Integer.class);
        IFeature f2 = fm.mutate().addFeature("f2");
        f2.mutate().setType(Integer.class);
        IFeature f3 = fm.mutate().addFeature("f3");
        f3.mutate().setType(String.class);
        IFeature f4 = fm.mutate().addFeature("f4");
        f4.mutate().setType(String.class);
        IFeature f5 = fm.mutate().addFeature("f5");
        f5.mutate().setType(Float.class);
        IFeature f6 = fm.mutate().addFeature("f6");
        f6.mutate().setType(Float.class);

        IConfiguration config = new Configuration(standardConfigEntries);
        Assertions.assertTrue(config.fitsToFeatureModel(fm));
    }

    @Test
    public void testNotFitsToFeatureModel() {
        IFeatureModel fm = new FeatureModel();
        IFeature f1 = fm.mutate().addFeature("f1");
        f1.mutate().setType(Integer.class);
        IFeature f2 = fm.mutate().addFeature("f2");
        f2.mutate().setType(Integer.class);
        IFeature f3 = fm.mutate().addFeature("f3");
        f3.mutate().setType(Integer.class);
        IFeature f4 = fm.mutate().addFeature("f4");
        f4.mutate().setType(String.class);
        IFeature f5 = fm.mutate().addFeature("f5");
        f5.mutate().setType(Float.class);
        IFeature f6 = fm.mutate().addFeature("f6");
        f6.mutate().setType(Float.class);

        IConfiguration config = new Configuration(standardConfigEntries);
        Assertions.assertFalse(config.fitsToFeatureModel(fm));
    }

    @Test
    public void testNotFitsToFeatureModel2() {
        IFeatureModel fm = new FeatureModel();
        IFeature f1 = fm.mutate().addFeature("f1");
        f1.mutate().setType(Integer.class);
        IFeature f2 = fm.mutate().addFeature("f2");
        f2.mutate().setType(Integer.class);
        IFeature f3 = fm.mutate().addFeature("f3");
        f3.mutate().setType(String.class);
        IFeature f4 = fm.mutate().addFeature("f4");
        f4.mutate().setType(String.class);
        IFeature f5 = fm.mutate().addFeature("f5");
        f5.mutate().setType(Float.class);
        IFeature f6 = fm.mutate().addFeature("f7");
        f6.mutate().setType(Float.class);

        IConfiguration config = new Configuration(standardConfigEntries);
        Assertions.assertFalse(config.fitsToFeatureModel(fm));
    }

    @Test
    public void testNotFitsToFeatureModel3() {
        IFeatureModel fm = new FeatureModel();
        IFeature f1 = fm.mutate().addFeature("f1");
        f1.mutate().setType(Integer.class);
        IFeature f2 = fm.mutate().addFeature("f2");
        f2.mutate().setType(Integer.class);
        IFeature f3 = fm.mutate().addFeature("f3");
        f3.mutate().setType(String.class);
        IFeature f4 = fm.mutate().addFeature("f4");
        f4.mutate().setType(String.class);
        IFeature f5 = fm.mutate().addFeature("f5");
        f5.mutate().setType(Float.class);

        IConfiguration config = new Configuration(standardConfigEntries);
        Assertions.assertFalse(config.fitsToFeatureModel(fm));
    }
}
