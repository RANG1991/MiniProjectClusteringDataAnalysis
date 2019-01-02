package Clustering;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Shape;
import java.util.*;
import javax.swing.JPanel;
import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.ShapeUtilities;

public class PointsPlotter extends ApplicationFrame {

    private HashMap<Integer, HashSet<double[]>> mapOfPoints;

    public PointsPlotter(String s, HashMap<Integer, HashSet<double[]>> mapOfPoints) {
        super(s);
        this.mapOfPoints = mapOfPoints;
        JPanel jpanel = createDemoPanel();
        jpanel.setPreferredSize(new Dimension(500, 270));
        setContentPane(jpanel);
    }

    public JPanel createDemoPanel() {

        JFreeChart jfreechart = ChartFactory.createScatterPlot("Scatter Plot Demo",
                "X", "Y", samplexydataset2(), PlotOrientation.VERTICAL, true, true, false);
        Shape cross = ShapeUtilities.createDiagonalCross(3, 1);

        XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
        XYItemRenderer renderer = xyPlot.getRenderer();
        renderer.setBaseShape(cross);
        renderer.setBasePaint(Color.red);
        //changing the Renderer to XYDotRenderer
        //xyPlot.setRenderer(new XYDotRenderer());
        XYDotRenderer xydotrenderer = new XYDotRenderer();
        xyPlot.setRenderer(xydotrenderer);
        xydotrenderer.setSeriesShape(0, cross);

        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);

        return new ChartPanel(jfreechart);
    }

    private XYDataset samplexydataset2() {
        int cols = 20;
        int rows = 20;
        double[][] values = new double[cols][rows];

        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
        XYSeries series = new XYSeries("Random");
        for (Integer centroid : this.mapOfPoints.keySet()) {
          for (double[] point : this.mapOfPoints.get(centroid))
          {
              series.add(point[0], point[1]);
          }
        }

        xySeriesCollection.addSeries(series);
        return xySeriesCollection;
    }

    public static void main(HashMap<Integer, HashSet<double[]>> mapOfPoints) {
        PointsPlotter scatterplotdemo4 = new PointsPlotter("Scatter Plot Demo 4", mapOfPoints);
        scatterplotdemo4.pack();
        RefineryUtilities.centerFrameOnScreen(scatterplotdemo4);
        scatterplotdemo4.setVisible(true);
    }
}
