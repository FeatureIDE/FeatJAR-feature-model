package de.featjar.feature.model.analysis.visualization;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.tree.Trees;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.analysis.AnalysisTree;
import de.featjar.feature.model.analysis.visitor.AnalysisTreeVisitor;
import de.rototor.pdfbox.graphics2d.PdfBoxGraphics2D;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.util.Matrix;
import org.knowm.xchart.*;
import org.knowm.xchart.internal.chartpart.Chart;

import javax.swing.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A class that builds a visualization using the XChart library.
 * Data is read as an {@link AnalysisTree} and the chart is built by the buildChart() methods of each child class.
 *
 * @author Benjamin von Holt
 * @author Valentin Laubsch
 */
public abstract class AVisualizeFeatureModelStats {

    final protected AnalysisTree<?> analysisTree;
    protected LinkedHashMap<String, LinkedHashMap<String, Object>> analysisTreeData;
    final protected ArrayList<Chart<?, ?>> charts;

    private Integer chartWidth = 800;
    private Integer chartHeight = 600;

    public AVisualizeFeatureModelStats(AnalysisTree<?> analysisTree) {
        this.analysisTree = analysisTree;
        this.analysisTreeData = extractAnalysisTree();
        this.charts = buildCharts();
        chartsAreEmpty();
    }

    public ArrayList<Chart<?, ?>> getCharts() {
        return this.charts;
    }

    public Integer getWidth() {
        return this.chartWidth;
    }

    public void setWidth(Integer width) {
        this.chartWidth = width;
    }

    public Integer getHeight() {
        return this.chartHeight;
    }

    public void setHeight(Integer height) {
        this.chartHeight = height;
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

    private boolean chartsAreEmpty() {return chartsAreEmpty("");}

    private boolean chartsAreEmptyDisplay() {
        return chartsAreEmpty("Cannot display chart: ");
    }

    private boolean chartsAreEmptyPDF() {
        return chartsAreEmpty("Cannot export chart to PDF: ");
    }

    /**
     * {@return String key used to fetch data from the Analysis Tree.}
     */
    protected abstract String getAnalysisTreeDataName();

    /**
     * Uses internal analysisTree and getAnalysisTreeDataName() to fetch the needed piece of data from the analysis tree.
     * <p>
     * {@return alphabetically sorted map with one key per tree.
     * Each value is another HashMap with one entry per piece of data extracted from the Analysis Tree. These pieces of data are
     * also alphabetically sorted.}
     */
    public LinkedHashMap<String, LinkedHashMap<String, Object>> extractAnalysisTree() throws RuntimeException {
        HashMap<String, Object> analysisMap = extractAnalysisMap();

        // fetches keys for all trees for the data we want
        // example: [Tree 1] Group Distribution, [Tree 2] Group Distribution, ...
        List<String> featureTreeDataKeys = analysisMap.keySet().stream()
                .filter(key -> key.contains(this.getAnalysisTreeDataName()))
                .sorted()
                .collect(Collectors.toList());

        // preparing return value
        LinkedHashMap<String, LinkedHashMap<String, Object>> analysisTreeData = new LinkedHashMap<>();

        // for each tree: add a HashMap containing values per piece of information we need to extract
        for (String key : featureTreeDataKeys) {
            LinkedHashMap<String, Object> featureTreeData = new LinkedHashMap<>();
            Object pieceOfInformation = analysisMap.get(key);
            assert pieceOfInformation != null : "Could not retrieve data called \"" + key + "\" from AnalysisTree.";

            if (pieceOfInformation instanceof Map) {
                @SuppressWarnings("unchecked")
                HashMap<String, Object> nestedMap = (HashMap<String, Object>) pieceOfInformation;

                nestedMap.keySet().stream()
                        .sorted()
                        .forEach(nestedMapKey -> {
                            // the value relevant for us needs to be unpacked first
                            Object rawValue = nestedMap.get(nestedMapKey);
                            ArrayList<?> castedValue = (ArrayList<?>) rawValue;
                            Object value = castedValue.get(2);
                            featureTreeData.put(nestedMapKey, value);
                        });

            } else if (pieceOfInformation instanceof ArrayList) {
                ArrayList<?> castedValue = (ArrayList<?>) pieceOfInformation;
                Object value = castedValue.get(2);
                featureTreeData.put(getAnalysisTreeDataName(), value);
            } else {
                throw new RuntimeException("Analysis Tree contained unknown data type in key " + key);
            }
            analysisTreeData.put(key, featureTreeData);
        }
        return analysisTreeData;
    }

    /**
     * {@return the {@link AnalysisTree}'s general HashMap that more specific information is stored in.}
     */
    private HashMap<String, Object> extractAnalysisMap() {
        Result<HashMap<String, Object>> result = Trees.traverse(analysisTree, new AnalysisTreeVisitor());
        HashMap<String, Object> receivedResult = result.get();
        assert receivedResult != null: "Analysis Tree Visitor failed to produce a result.";

        // we currently trust that this is always "Analysis"
        @SuppressWarnings("unchecked")
        HashMap<String, Object> analysisMap = (HashMap<String, Object>) receivedResult.get("Analysis");
        assert analysisMap != null: "Received no \"Analysis\" HashMap from AnalysisTree";

        return analysisMap;
    }

    /**
     * Use analysisTreeData to access the data relevant for building your chart.
     * There are also premade builders that you may adopt.
     * @return list containing one chart per tree in the feature model
     */
    abstract ArrayList<Chart<?, ?>> buildCharts();

    /**
     * Premade builder for pie charts that you can use when implementing buildCharts().
     * @return list containing one chart per tree in the feature model
     */
    protected ArrayList<Chart<?, ?>> buildPieCharts() {
        ArrayList<Chart<?, ?>> charts = new ArrayList<>();
        for (String treeKey : this.analysisTreeData.keySet()) {
            PieChart chart = new PieChartBuilder()
                    .width(getWidth())
                    .height(getHeight())
                    .build();
            chart.setTitle(treeKey);

            HashMap<String, Object> treeData = analysisTreeData.get(treeKey);
            treeData.forEach((key, value) -> chart.addSeries(key, (Integer) value));

            charts.add(chart);
        }
        return charts;
    }

    /**
     * Premade builder for box charts that you can use when implementing buildCharts().
     * @return list containing one chart per tree in the feature model
     */
    protected ArrayList<Chart<?, ?>> buildBoxCharts() {
        ArrayList<Chart<?, ?>> charts = new ArrayList<>();
        // in averagenumberofchildren only one double is provided instead of an double array, so I used testdata to test die BoxPlot
        // for example not the average number of children, but the number of children for every node as a double or int array is needed to plot a boxplot for the average number of children
        double[] testdata = {0.9, 1.5, 2.22};

        for (String treeKey : this.analysisTreeData.keySet()) {
            // Momentan bauen wir pro Tree einen Chart mit einer Box mit mehreren Werten
            // Wäre es für Boxplots nicht sinnvoller einen Chart für alle Trees zu machen,
            // mit je eine Box pro Tree?
            BoxChart chart = new BoxChartBuilder()
                    .width(getWidth())
                    .height(getHeight())
                    .build();
            chart.setTitle(treeKey);

            HashMap<String, Object> treeData = analysisTreeData.get(treeKey);

            treeData.forEach((key, value) -> chart.addSeries(key, testdata));

            charts.add(chart);
        }
        return charts;
    }

    /**
     * Creates a live preview pop-up window of a chart.
     * @param chart the chart that will be displayed
     * @return 0 on success, 1 on general error
     */
    public int displayChart (Chart<?, ?> chart) {
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
        if (chartsAreEmptyDisplay()) {return 2;}
        return this.displayChart(0);
    }

    /**
     * Creates a live preview pop-up window of an internally generated chart, fetched by index.
     * @return 0 on success, 1 on general error, 2 on empty internal chart list
     */
    public int displayChart (Integer index) {
        if (chartsAreEmptyDisplay()) {return 2;}
        try {
            this.displayChart(this.charts.get(index));
        } catch (IndexOutOfBoundsException e) {
            FeatJAR.log().error("Unable to fetch chart with index "+ index + ": " + e);
            return 1;
        }
        return 0;
    }

    /**
     * Creates live preview pop-up windows of ALL internally generated charts.
     * @return 0 on success, 1 on general error, 2 on empty internal chart list
     */
    public int displayAllCharts() {
        if (chartsAreEmptyDisplay()) {return 2;}

        int returnValue = 0;

        for (Chart<?, ?> chart : this.charts) {
            returnValue = this.displayChart(chart);
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
    private int exportChartToPDF(Chart<?, ?> chart, PDDocument document) {
        PDPage page = new PDPage();
        document.addPage(page);

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
            Matrix transformationMatrix = getPDFCenteringMatrix(page);
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
    public int exportChartToPDF(Chart<?, ?> chart, String path) {
        return exportAllChartsToPDF(Collections.singletonList(chart), path);
    }

    /**
     * Creates a PDF document with a single page featuring the specified chart.
     * @param index index to retrieve a chart form the internal chart list
     * @param path  full path to the destination file. Does not check whether you specified an extension.
     * @return 0 on success, 1 on general errors, 2 if there are no internal charts to use.
     */
    public int exportChartToPDF(Integer index, String path) {
        if (chartsAreEmptyDisplay()) {return 2;}
        try {
            return exportChartToPDF(charts.get(index), path);
        } catch (IndexOutOfBoundsException e) {
            FeatJAR.log().error("Unable to fetch chart with index "+ index + ": " + e);
            return 1;
        }
    }

    /**
     * Creates a PDF document with a single page featuring the first chart from the internal list.
     * @param path  full path to the destination file. Does not check whether you specified an extension.
     * @return 0 on success, 1 on general errors, 2 if there are no internal charts to use.
     */
    public int exportChartToPDF(String path) {
        if (chartsAreEmptyDisplay()) {return 2;}
        return exportChartToPDF(0, path);
    }

    /**
     * Creates a new PDF document and fills it with pages that each feature one chart from the list
     * @param charts   any list of charts
     * @param path     full path to the destination file. Does not check whether you specified an extension.
     * @return 0 on success, 1 otherwise
     */
    public int exportAllChartsToPDF(List<Chart<?, ?>> charts, String path) {
        try (PDDocument document = new PDDocument()) {

            int iExitCode;
            for (Chart<?, ?> chart : charts) {
                iExitCode = exportChartToPDF(chart, document);
                if (iExitCode != 0) {
                    return 1;
                }
            }
            document.save(path);
            return 0;

        } catch (IOException e) {

            FeatJAR.log().error(e);
            return 1;

        }
    }

    /**
     * Creates a new PDF document and fills it with pages that each feature one chart from the list
     * @param path     full path to the destination file. Does not check whether you specified an extension.
     * @return 0 on success, 1 on general errors, 2 if there are no internal charts to use.
     */
    public int exportAllChartsToPDF(String path) {
        if (chartsAreEmptyDisplay()) {return 2;}
        return exportAllChartsToPDF(charts, path);
    }

    /**
     * @param page
     * {@return transformation matrix that scales a given chart to fill the page and be centered}
     */
    private Matrix getPDFCenteringMatrix(PDPage page) {
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
