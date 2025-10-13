package de.featjar.feature.model.analysis.visualization;

import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.PieChart;
import org.knowm.xchart.PieChartBuilder;

public class VisualizeFeatureModelStats {

    private double[] xData;
    private double[] yData;

    private String chartTitle;
    private String xTitle;
    private String yTitle;
    private String seriesName = "y(x)";

    public VisualizeFeatureModelStats(double[] xData, double[] yData) {
        this.xData = xData;
        this.yData = yData;
        this.chartTitle = "Titel";
        this.xTitle = "x-Achse";
        this.yTitle = "y-Achse";
    }

    public VisualizeFeatureModelStats(double[] xData, double[] yData, String chartTitle, String xTitle, String yTitle) {
        this.xData = xData;
        this.yData = yData;
        this.chartTitle = chartTitle;
        this.xTitle = xTitle;
        this.yTitle = yTitle;
    }

    public XYChart getChart() {
        return QuickChart.getChart
                (this.chartTitle, this.xTitle, this.yTitle, this.seriesName, this.xData, this.yData);
    }

    public void displayChart() {
        //new Swing
    }
    public static void main(String[] args) {
        // Sample data
        double[] xData = new double[] {0.0, 1.0, 2.0};
        double[] yData = new double[] {2.0, 1.0, 0.0};

        // Create Chart
        XYChart chart = QuickChart.getChart("Sample Chart", "X", "Y", "y(x)", xData, yData);
        PieChart chart1 = new PieChartBuilder().build();

        chart1.addSeries("Banananana", 40);
        chart1.addSeries("Apple", 25);
        chart1.addSeries("Gurke", 50);

        // Display the chart
        new SwingWrapper<>(chart1).displayChart();
    }
}
