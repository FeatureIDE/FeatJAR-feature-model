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
package de.featjar.feature.model.analysis.visualization;

import de.featjar.base.FeatJAR;
import de.featjar.base.tree.Trees;
import de.featjar.feature.model.analysis.AnalysisTree;
import de.featjar.feature.model.analysis.visitor.AnalysisTreeKeywordTreeVisitor;
import de.rototor.pdfbox.graphics2d.PdfBoxGraphics2D;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import javax.swing.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.util.Matrix;
import org.knowm.xchart.*;
import org.knowm.xchart.internal.chartpart.Chart;

/**
 * Visualizes and exports feature model statistics.
 * Data is read as an {@link AnalysisTree}. Each child specifies the information to be read from the tree via
 * {@link #getAnalysisTreeDataName()}, as well as how to build a chart from it via the {@link #buildCharts()} method.
 *
 * @author Benjamin von Holt
 * @author Valentin Laubsch
 */
public abstract class AVisualizeFeatureModelStats {

    protected final AnalysisTree<?> analysisTree;
    protected LinkedHashMap<String, LinkedHashMap<String, Object>> analysisTreeData;
    protected ArrayList<Chart<?, ?>> charts;

    private String chartTitle = null;
    private int chartWidth = 800;
    private int chartHeight = 600;
    protected Font fontTitle = new Font(Font.SANS_SERIF, Font.BOLD, 30);
    protected Font fontLabels = new Font(Font.SANS_SERIF, Font.BOLD, 20);
    protected Font fontLegend = new Font(Font.SANS_SERIF, Font.PLAIN, 20);

    /**
     * Visualizes and exports feature model statistics.
     *
     * @param analysisTree {@link AnalysisTree} over the entire feature model.
     */
    public AVisualizeFeatureModelStats(AnalysisTree<?> analysisTree) {
        this.analysisTree = analysisTree;
        this.analysisTreeData = extractAnalysisTree();
        this.charts = buildCharts();
        chartsAreEmpty();
    }

    public ArrayList<Chart<?, ?>> getCharts() {
        return this.charts;
    }

    public void setCharts(ArrayList<Chart<?, ?>> charts) {
        this.charts = charts;
    }

    public String getChartTitle() {
        return chartTitle;
    }

    public void setChartTitle(String chartTitle) {
        this.chartTitle = chartTitle;
        this.charts = buildCharts();
    }

    public int getWidth() {
        return this.chartWidth;
    }

    public void setWidth(int width) {
        this.chartWidth = width;
        this.charts = buildCharts();
    }

    public int getHeight() {
        return this.chartHeight;
    }

    public void setHeight(int height) {
        this.chartHeight = height;
        this.charts = buildCharts();
    }

    /**
     * Checks if any charts are available and if not reminds the user of this fact with a warning message.
     * @param extraMessage Will be logged before the main message, for example to specify what task cannot be completed
     * @return True if charts exist, else false.
     */
    protected boolean chartsAreEmpty(String extraMessage) {

        if (this.charts.isEmpty()) {
            FeatJAR.log().warning(extraMessage + this.getClass().getName() + " did not build any charts!");
            return true;
        }
        return false;
    }

    private boolean chartsAreEmpty() {
        return chartsAreEmpty("");
    }

    private boolean chartsAreEmptyDisplay() {
        return chartsAreEmpty("Cannot display chart: ");
    }

    private boolean chartsAreEmptyPDF() {
        return chartsAreEmpty("Cannot export chart to PDF: ");
    }

    /**
     * {@return String key used to fetch data from {@link #analysisTree}.}
     */
    protected abstract String getAnalysisTreeDataName();

    /**
     * Uses internal analysisTree and getAnalysisTreeDataName() to fetch the needed piece of data from the analysis tree.
     * <p>
     * {@return alphabetically sorted map with one key per tree.
     * Each value is another HashMap with one entry per piece of data extracted from the Analysis Tree. These pieces of data are
     * also alphabetically sorted.}
     */
    public LinkedHashMap<String, LinkedHashMap<String, Object>> extractAnalysisTree() {
        HashMap<String, Object> relevantAnalysisSubTrees =
                Trees.traverse(analysisTree, new AnalysisTreeKeywordTreeVisitor(getAnalysisTreeDataName())).get();

        // preparing return value
        LinkedHashMap<String, LinkedHashMap<String, Object>> analysisTreeData = new LinkedHashMap<>();

        // for each tree: add a HashMap containing values per piece of information we need to extract
        for (String key : relevantAnalysisSubTrees.keySet()) {
            Object value = relevantAnalysisSubTrees.get(key);
            assert value != null : "Could not retrieve data called \"" + key + "\" from AnalysisTree.";

            analysisTreeData.put(key , new LinkedHashMap<>());

            if (value instanceof List) {
                @SuppressWarnings("unchecked")
                List<AnalysisTree<?>> childAnalysisTrees = (List<AnalysisTree<?>>) value;
                for (AnalysisTree<?> childTree: childAnalysisTrees) {
                    analysisTreeData.get(key).put(childTree.getName(), childTree.getValue());
                }
            } else if (value instanceof Number) {
                analysisTreeData.get(key).put(key, value);
            }
        }
        return analysisTreeData;
    }

    /**
     * Uses {@link #analysisTreeData} to access the data relevant for building your chart.
     * There are also premade builders that you may adopt, such as {@link #buildPieCharts()}.
     * @return list containing one chart per tree in the feature model
     */
    protected abstract ArrayList<Chart<?, ?>> buildCharts();

    /**
     * Premade builder for pie charts that you can use when implementing {@link #buildCharts()}.
     * @return list containing one chart per tree in the feature model
     */
    protected ArrayList<Chart<?, ?>> buildPieCharts() {
        ArrayList<Chart<?, ?>> charts = new ArrayList<>();
        for (String treeKey : this.analysisTreeData.keySet()) {
            PieChart chart =
                    new PieChartBuilder().width(getWidth()).height(getHeight()).build();
            chart.setTitle((this.chartTitle == null) ? treeKey : this.chartTitle);
            defaultStyler(chart);

            HashMap<String, Object> treeData = analysisTreeData.get(treeKey);

            try {
                treeData.forEach((key, value) -> chart.addSeries(key, (Number) value));
            } catch (ClassCastException e) {
                FeatJAR.log().error("Invalid data for pie chart: " + e);
                continue;
            }

            boolean anyNegativeValues =
                    treeData.values().stream().map(value -> (Number) value).anyMatch(value -> value.doubleValue() < 0);
            if (anyNegativeValues) {
                FeatJAR.log().error("Pie chart cannot be built with negative values");
                continue;
            }
            charts.add(chart);
        }
        return charts;
    }

    /**
     * Styles charts with the default settings, for consistency.
     */
    private void defaultStyler(Chart<?, ?> chart) {
        chart.getStyler().setChartTitleFont(fontTitle);
        chart.getStyler().setLegendFont(fontLegend);
        if (chart instanceof PieChart) {
            defaultStylerPieChartExtension((PieChart) chart);
        }
    }

    /**
     * extends {@link #defaultStyler(Chart)} with PieChart-specific calls
     */
    private void defaultStylerPieChartExtension(PieChart chart) {
        chart.getStyler().setLabelsFont(fontLabels);
    }

    /**
     * Creates a live preview pop-up window of a chart.
     * @param chart the chart that will be displayed
     * @return 0 on success, 1 on general error
     */
    public static int displayChart(Chart<?, ?> chart) {
        try {
            JFrame jframe = new SwingWrapper<>(chart).displayChart();
            if (jframe == null) {
                FeatJAR.log().error("Could not display chart! Received null object from XChart SwingWrapper.");
                return 1;
            }
        } catch (Exception e) {
            FeatJAR.log().error("Could not display chart!" + e);
            return 1;
        }
        return 0;
    }

    /**
     * Creates a live preview pop-up window of the FIRST internally generated chart.
     * This chart usually corresponds to the first feature tree in the feature model.
     * @return 0 on success, 1 on general error, 2 on empty internal chart list
     */
    public int displayChart() {
        if (chartsAreEmptyDisplay()) {
            return 2;
        }
        return this.displayChart(0);
    }

    /**
     * Creates a live preview pop-up window of an internally generated chart, fetched by index.
     * @return 0 on success, 1 on general error, 2 on empty internal chart list
     */
    public int displayChart(int index) {
        if (chartsAreEmptyDisplay()) {
            return 2;
        }
        try {
            displayChart(this.charts.get(index));
        } catch (IndexOutOfBoundsException e) {
            FeatJAR.log().error("Unable to fetch chart with index " + index + ": " + e);
            return 1;
        }
        return 0;
    }

    /**
     * Creates live preview pop-up windows of ALL internally generated {@link #charts}.
     * @return 0 on success, 1 on general error, 2 on empty internal chart list
     */
    public int displayAllCharts() {
        return displayAllCharts(charts);
    }

    /**
     * Creates live preview pop-up windows of ALL charts passed as argument
     * @return 0 on success, 1 on general error, 2 on empty internal chart list
     */
    public static int displayAllCharts(ArrayList<Chart<?, ?>> charts) {
        if (charts.isEmpty()) {
            return 2;
        }

        int returnValue = 0;

        for (Chart<?, ?> chart : charts) {
            returnValue = displayChart(chart);
            if (returnValue != 0) {
                break;
            }
        }
        return returnValue;
    }

    /**
     * Takes an existing PDF document and adds a new page with the specified chart on it.
     * @param chart    the chart that will be added on its own page
     * @param document existing PDF document
     * @return 0 on success, 1 on IOException
     */
    private static int exportChartToPDF(Chart<?, ?> chart, PDDocument document) {
        PDPage page = new PDPage();
        document.addPage(page);
        int chartWidth = chart.getWidth();
        int chartHeight = chart.getHeight();

        // Create a PdfBoxGraphics2D object and draw the chart into it
        PdfBoxGraphics2D graphics;
        try {
            graphics = new PdfBoxGraphics2D(document, chartWidth, chartHeight);
        } catch (IOException e) {
            FeatJAR.log().error("PDF Exporter could not create PdfBoxGraphics2D object: " + e);
            return 1;
        }
        chart.paint(graphics, chartWidth, chartHeight);
        graphics.dispose();

        // transforms the chart so it fits on the page, then draws it
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            Matrix transformationMatrix = getPDFCenteringMatrix(page, chartWidth, chartHeight);
            contentStream.transform(transformationMatrix);
            contentStream.drawForm(graphics.getXFormObject());
        } catch (IOException e) {
            FeatJAR.log().error("PDF Exporter failed to add a chart to its PDF page: " + e);
            return 1;
        }

        return 0;
    }

    /**
     * Creates a PDF document with a single page featuring the specified chart.
     * @param chart chart that will be put on the page
     * @param path  full path to the destination file. Does not check whether you specified an extension.
     * @return 0 on success, 1 otherwise.
     */
    public static int exportChartToPDF(Chart<?, ?> chart, String path) {
        return exportAllChartsToPDF(Collections.singletonList(chart), path);
    }

    /**
     * Creates a PDF document with a single page featuring the specified chart.
     * @param index index to retrieve a chart form the internal chart list
     * @param path  full path to the destination file. Does not check whether you specified an extension.
     * @return 0 on success, 1 on general errors, 2 if there are no internal {@link #charts} to use.
     */
    public int exportChartToPDF(int index, String path) {
        if (chartsAreEmptyPDF()) {
            return 2;
        }
        try {
            return exportChartToPDF(charts.get(index), path);
        } catch (IndexOutOfBoundsException e) {
            FeatJAR.log().error("Unable to fetch chart with index " + index + ": " + e);
            return 1;
        }
    }

    /**
     * Creates a PDF document with a single page featuring the first chart from the internal list.
     * @param path  full path to the destination file. Does not check whether you specified an extension.
     * @return 0 on success, 1 on general errors, 2 if there are no internal {@link #charts} to use.
     */
    public int exportChartToPDF(String path) {
        if (chartsAreEmptyPDF()) {
            return 2;
        }
        return exportChartToPDF(0, path);
    }

    /**
     * Creates a new PDF document and fills it with pages that each feature one chart from the list
     * @param charts   any list of charts
     * @param path     full path to the destination file. Does not check whether you specified an extension.
     * @return 0 on success, 1 otherwise
     */
    public static int exportAllChartsToPDF(List<Chart<?, ?>> charts, String path) {
        try (PDDocument document = new PDDocument()) {

            int iExitCode;
            for (Chart<?, ?> chart : charts) {
                iExitCode = exportChartToPDF(chart, document);
                if (iExitCode != 0) {
                    return 1;
                }
            }

            // create folder if it does not already exist
            Path pathToFolder = Paths.get(path).getParent();
            if (pathToFolder != null && !Files.exists(pathToFolder)) {
                Files.createDirectories(pathToFolder);
            }

            document.save(path);
            return 0;

        } catch (IOException | InvalidPathException e) {
            FeatJAR.log().error(e);
            return 1;
        }
    }

    /**
     * Creates a new PDF document and fills it with pages that each feature one chart from the list
     * @param path     full path to the destination file. Does not check whether you specified an extension.
     * @return 0 on success, 1 on general errors, 2 if there are no internal {@link #charts} to use.
     */
    public int exportAllChartsToPDF(String path) {
        if (chartsAreEmptyPDF()) {
            return 2;
        }
        return exportAllChartsToPDF(charts, path);
    }

    /**
     * {@return transformation matrix that scales a given chart to fill the page and be centered}
     */
    private static Matrix getPDFCenteringMatrix(PDPage page, int chartWidth, int chartHeight) {
        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();

        // calculate scale to fit chart to page (preserving aspect ratio)
        float scaleX = pageWidth / chartWidth;
        float scaleY = pageHeight / chartHeight;
        float minScale = Math.min(scaleX, scaleY);

        // coordinates for centering chart
        float translateX = (pageWidth - chartWidth * minScale) / 2;
        float translateY = (pageHeight - chartHeight * minScale) / 2;

        // create transform matrix with scale and translation
        AffineTransform at = new AffineTransform(); // affine transformations preserve distance ratios
        at.translate(translateX, translateY);
        at.scale(minScale, minScale);

        return new Matrix(at);
    }
}
