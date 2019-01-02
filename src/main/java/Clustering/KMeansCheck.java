package Clustering;

import java.awt.Graphics2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KMeansCheck {

    public static void main(String[] args)
    {
        KMeans(3, "C:\\Users\\Admin\\Desktop\\KMeansCheck\\points\\points.txt",
                "C:\\Users\\Admin\\Desktop\\KMeansCheck\\centroids\\mu.txt");
    }


    public static ArrayList<double[]> generateRandomCentroids(String fileName)
    {
        ArrayList<double[]> centroids = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            for (String line : stream.collect(Collectors.toList())) {
                double[] centroid = new double[2];
                String[] rowArray = line.split("\t");
                centroid[0] = Double.valueOf(rowArray[0]);
                centroid[1] = Double.valueOf(rowArray[1]);
                centroids.add(centroid);
            }
        }
        catch (IOException e)
            {
                e.printStackTrace();
            }
        return centroids;
    }

    public static ArrayList<double[]> generatePoints(String fileName)
    {
        ArrayList<double[]> points = new ArrayList<>();
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            for (String line : stream.collect(Collectors.toList())) {
                double[] point = new double[2];
                String[] rowArray = line.split("\t");
                point[0] = Double.valueOf(rowArray[0]);
                point[1] = Double.valueOf(rowArray[1]);
                points.add(point);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return points;
    }


    public static double calculateDistance(double[] vector1, double[] vector2)
    {
        double sum = 0;
        for (int i = 0 ; i < vector2.length ; i++)
        {
            sum = sum + Math.pow(vector1[i] - vector2[i], 2);
        }
        return Math.sqrt(sum);
    }

    public static double[] calculateNewCentroid(double[] centroid, HashSet<double[]> points)
    {
        double[] newCentroid = new double[centroid.length];
        if (points.size() > 0)
        {
            for (int i = 0 ; i < centroid.length ; i++) {
                double sum = 0;
                for (double[] point : points) {
                    sum = sum + point[i];
                }
                newCentroid[i] = sum / points.size();
            }
            return newCentroid;
        }
        else
        {
            return centroid;
        }
    }

    public static void KMeans(int k, String fileNamePoints, String fileNameCentroids)
    {
        ArrayList<double[]> centroids = generateRandomCentroids(fileNameCentroids);
        ArrayList<double[]> points = generatePoints(fileNamePoints);
        HashMap<Integer, HashSet<double[]>> centroidsGroups = null;
        for (int j = 0 ; j < 1000 ; j++)
        {
            centroidsGroups = new HashMap<>();
            for (int i = 0 ; i < k ; i++)
            {
                centroidsGroups.put(i, new HashSet<>());
            }
            for (double[] point : points)
            {
                    double minDist = Double.MAX_VALUE;
                    int minIndexOfCentroids = -1;
                    for (int l = 0; l < k; l++) {
                        double dist = calculateDistance(point, centroids.get(l));
                        if (dist <= minDist) {
                            minIndexOfCentroids = l;
                            minDist = dist;
                        }
                    }
                    centroidsGroups.get(minIndexOfCentroids).add(point);
            }

            ArrayList<double[]> newCentroids = new ArrayList<>();
            for (int i = 0; i < k; i++)
            {
                newCentroids.add(calculateNewCentroid(centroids.get(i), centroidsGroups.get(i)));
            }
            centroids = new  ArrayList<>(newCentroids);
        }
        for (Integer centroid : centroidsGroups.keySet())
        {
            System.out.println((centroidsGroups.get(centroid).size()));
        }
       PointsPlotter.main(centroidsGroups);
    }
}
