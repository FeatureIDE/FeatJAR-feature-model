package de.featjar.feature.model.io.csv;

import de.featjar.base.data.identifier.Identifiers;
import de.featjar.base.io.IO;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.analysis.SimpleTreeProperties;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;


/**
 * This is a temp class; the real implementation will be done via IFormat implementation. It will be deleted later.
 */
public class CSVExporter {
    // should probably pull this from the IO Exporter / Importer
    public final String DELIMITER = ";";
    public final Charset DEFAULT_CHARSET = IO.DEFAULT_CHARSET;

    public IFeatureTree makeTree () {
        FeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
        IFeatureTree rootTree =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
        rootTree.mutate().toAlternativeGroup();

        IFeature childFeature1 = featureModel.mutate().addFeature("Root's Child (in AltGroup)");
        IFeatureTree childTree1 = rootTree.mutate().addFeatureBelow(childFeature1);
        IFeature childFeature2 = featureModel.mutate().addFeature("1st Child of Root's Child");
        childTree1.mutate().addFeatureBelow(childFeature2);
        IFeature childFeature3 = featureModel.mutate().addFeature("2nd Child of Root's Child");
        childTree1.mutate().addFeatureBelow(childFeature3);

        return rootTree;
    }

    // change formatting here
    private String roundAndCastToString (int numerator, int denominator) {
        return String.format("%.2f", (float) numerator / denominator);
    }

    private String roundAndCastToString (float number) {
        return String.format("%.2f", number);
    }

    public LinkedHashMap<String, Object> gatherStatistics (IFeatureTree tree) {
        SimpleTreeProperties simpleTreeProperties = new SimpleTreeProperties();

        int topFeatures = simpleTreeProperties.topFeatures(tree).get();
        int leafFeatures = simpleTreeProperties.leafFeaturesCounter(tree).get();
        int treeDepth = simpleTreeProperties.treeDepth(tree).get();

        HashMap<String, Integer> groupDistribution = simpleTreeProperties.groupDistribution(tree).get();
        int alternativeGroups = groupDistribution.get("AlternativeGroup");
        int orGroups = groupDistribution.get("OrGroup");
        int andGroups = groupDistribution.get("AndGroup");
        int allGroups = alternativeGroups + orGroups + andGroups;

        float avgNumberOfChildren = simpleTreeProperties.avgNumberOfChildren(tree).get();

        // linked map to preserve order for now, not sure if needed
        // todo: decide whether to make this a variable <String, Object> map, or a ready-to-write <String, String> map
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("Features Directly Below Root", topFeatures);
        map.put("Features That Have No Child Features", leafFeatures);
        map.put("Tree Depth", treeDepth);
        map.put("Share of Alternative Groups", roundAndCastToString(alternativeGroups, allGroups));
        map.put("Share of Or-Groups", roundAndCastToString(orGroups, allGroups));
        map.put("Share of And-Groups", roundAndCastToString(andGroups, allGroups));
        map.put("Average Number of Children", roundAndCastToString(avgNumberOfChildren));

        return map;
    }

    public void export(String[] csvStrings) {
        Path path = Paths.get("C:\\Users\\bentu\\Desktop\\myfile.csv");

        /*
        IO.write(
                String.join("\n", csvStrings),
                path,
                DEFAULT_CHARSET
        );

         */

    }

    public static void main(String[] args){
        CSVExporter csvExporter = new CSVExporter();
        IFeatureTree tree = csvExporter.makeTree();
        LinkedHashMap<String, Object> stats = csvExporter.gatherStatistics(tree);
        // outputs the stat titles in order
        String firstLine = String.join(csvExporter.DELIMITER, stats.keySet());

        // outputs the stat values in order
        String secondLine = stats.values().stream()
                .map(String::valueOf)  // safely converts all objects to String, including nulls
                .collect(Collectors.joining(csvExporter.DELIMITER));

        System.out.println(firstLine); // prints the column names
        System.out.println(secondLine); // prints the stats (for the first tree)

        // todo iformat, xmlformat

    }

}
