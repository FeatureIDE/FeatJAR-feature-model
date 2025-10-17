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
package de.featjar.feature.model.visualization;

import static org.junit.jupiter.api.Assertions.*;

import de.featjar.base.data.Result;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.base.io.IO;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.TestDataProvider;
import de.featjar.feature.model.analysis.AnalysisTree;
import de.featjar.feature.model.analysis.visualization.VisualizeConstraintOperatorDistribution;
import de.featjar.feature.model.analysis.visualization.VisualizeGroupDistribution;
import de.featjar.feature.model.cli.PrintStatistics;
import de.featjar.feature.model.io.transformer.AnalysisTreeTransformer;
import de.featjar.feature.model.io.xml.XMLFeatureModelFormat;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;

public class VisualizeFeatureModelStatsTest {
    AnalysisTree<?> bigTree = getBigAnalysisTree();
    AnalysisTree<?> mediumTree = getMediumAnalysisTree();
    AnalysisTree<?> doubleTree = getDoubleTree();

    String defaultExportName =
            "src/test/java/de/featjar/feature/model/visualization/VisualizeFeatureModelStatsTest.pdf";

    /**
     * Helper function. Converts a feature model into an {@link AnalysisTree}
     */
    public AnalysisTree<?> analysisTreeFromFeatureModel(FeatureModel featureModel) {
        PrintStatistics printStatistics = new PrintStatistics();
        LinkedHashMap<String, Object> map =
                printStatistics.collectStats(featureModel, PrintStatistics.AnalysesScope.ALL);
        return AnalysisTreeTransformer.hashMapToTree(map, "Analysis").get();
    }

    /**
     * Helper function. Converts an XML file into an {@link AnalysisTree}
     */
    public AnalysisTree<?> analysisTreeFromXML(Path path) {
        Result<IFeatureModel> load = IO.load(path, new XMLFeatureModelFormat());
        FeatureModel model = (FeatureModel) load.orElseThrow();

        PrintStatistics printStatistics = new PrintStatistics();
        LinkedHashMap<String, Object> map = printStatistics.collectStats(model, PrintStatistics.AnalysesScope.ALL);
        return AnalysisTreeTransformer.hashMapToTree(map, "Analysis").get();
    }

    public AnalysisTree<?> getBigAnalysisTree() {
        return analysisTreeFromXML(Paths.get("src/test/java/de/featjar/feature/model/visualization/model.xml"));
    }

    /**
     * Warning: this tree does not have Constraint Operators
     */
    public AnalysisTree<?> getMediumAnalysisTree() {
        return analysisTreeFromFeatureModel(TestDataProvider.createMediumFeatureModel());
    }

    /**
     * {@return Feature Model with two identical trees.}
     */
    public AnalysisTree<?> getDoubleTree() {
        FeatureModel featureModel = TestDataProvider.createMediumFeatureModel();
        featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("ConfigDB"));
        return analysisTreeFromFeatureModel(featureModel);
    }

    @Test
    void twoPagePDFExport() {
        VisualizeGroupDistribution vizGroup;
        vizGroup = new VisualizeGroupDistribution(doubleTree);
        assertEquals(2, vizGroup.getCharts().size());
        assertEquals(0, vizGroup.exportAllChartsToPDF(defaultExportName));
        assertTrue(Files.exists(Paths.get(defaultExportName)));
    }

    @Test
    void pdfValidIndexGroupDistribution() {
        VisualizeGroupDistribution vizGroup;

        vizGroup = new VisualizeGroupDistribution(mediumTree);
        assertEquals(0, vizGroup.exportChartToPDF(0, defaultExportName));
        assertTrue(Files.exists(Paths.get(defaultExportName)));

        vizGroup = new VisualizeGroupDistribution(bigTree);
        assertEquals(0, vizGroup.exportChartToPDF(0, defaultExportName));
        assertTrue(Files.exists(Paths.get(defaultExportName)));

        vizGroup = new VisualizeGroupDistribution(doubleTree);
        assertEquals(0, vizGroup.exportChartToPDF(1, defaultExportName));
        assertTrue(Files.exists(Paths.get(defaultExportName)));
    }

    @Test
    void pdfValidIndexOperatorDistribution() {
        VisualizeConstraintOperatorDistribution vizOpDis;
        vizOpDis = new VisualizeConstraintOperatorDistribution(bigTree);
        assertEquals(0, vizOpDis.exportChartToPDF(0, defaultExportName));
        assertTrue(Files.exists(Paths.get(defaultExportName)));
    }

    @Test
    void pdfInvalidIndex() {
        // todo question: is one test enough?
        VisualizeGroupDistribution vizGroup;
        vizGroup = new VisualizeGroupDistribution(mediumTree);
        assertEquals(1, vizGroup.exportChartToPDF(99, defaultExportName));
        vizGroup = new VisualizeGroupDistribution(bigTree);
        assertEquals(1, vizGroup.exportChartToPDF(99, defaultExportName));

        VisualizeConstraintOperatorDistribution vizOpDis;
        vizOpDis = new VisualizeConstraintOperatorDistribution(bigTree);
        assertEquals(1, vizOpDis.exportChartToPDF(99, defaultExportName));
    }

    @Test
    void changeChartHeight() {
        VisualizeGroupDistribution vizGroup;
        vizGroup = new VisualizeGroupDistribution(mediumTree);

        Integer chartHeight = vizGroup.getHeight();
        assertEquals(chartHeight, vizGroup.getCharts().get(0).getHeight());

        chartHeight = 500;
        vizGroup.setHeight(chartHeight);
        assertEquals(chartHeight, vizGroup.getCharts().get(0).getHeight());
    }

    @Test
    void changeChartWidth() {
        VisualizeGroupDistribution vizGroup;
        vizGroup = new VisualizeGroupDistribution(mediumTree);

        Integer chartWidth = vizGroup.getWidth();
        assertEquals(chartWidth, vizGroup.getCharts().get(0).getWidth());

        chartWidth = 500;
        vizGroup.setWidth(chartWidth);
        assertEquals(chartWidth, vizGroup.getCharts().get(0).getWidth());
    }

    @Test
    void invalidPDFPath() {
        VisualizeGroupDistribution vizGroup;
        vizGroup = new VisualizeGroupDistribution(mediumTree);
        assertEquals(1, vizGroup.exportChartToPDF("\0/x.xml"));
    }

    @Test
    void pdfExportWithFolderCreation() throws IOException {
        String[] allPaths = {
            "pdfExportWithFolderCreation.pdf",
            "Visualizer Test Folder/pdfExportWithFolderCreation.pdf",
            "Visualizer Test Folder/Nested Folder/pdfExportWithFolderCreation.pdf"
        };

        // cleanup
        for (String path : allPaths) {
            Files.deleteIfExists(Paths.get(path));
        }

        // actual tests
        VisualizeGroupDistribution vizGroup = new VisualizeGroupDistribution(mediumTree);
        for (String path : allPaths) {
            assertEquals(0, vizGroup.exportChartToPDF(path));
            Path castedPath = Paths.get(path);
            assertTrue(Files.exists(castedPath));
            File file = castedPath.toFile();
            assertTrue(file.exists() && file.isFile());
            assertTrue(file.length() > 1);
        }

        // clean up
        for (String path : allPaths) {
            Path castedPasted = Paths.get(path);
            Files.deleteIfExists(castedPasted);
        }
        Files.delete(Paths.get("Visualizer Test Folder/Nested Folder"));
        Files.delete(Paths.get("Visualizer Test Folder"));
    }
}
