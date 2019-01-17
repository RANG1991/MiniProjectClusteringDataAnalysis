package Clustering;

import java.util.*;

public class KmeansShakedImprovement {
    private Processing processing;
    private HashMap<Integer, double[]> movieVectors = new HashMap<>();
    private HashMap<double[], Integer> vectorMovies = new HashMap<>();
    private  HashMap<Integer, ArrayList<Integer>> correlation = new HashMap<>();
    private HashMap<AbstractMap.SimpleEntry<Integer, Integer>, Double> RelationFraction = new HashMap<>();
    private HashMap<Integer, Integer> clusterToMovie = new HashMap<>();
    private double[] p_mArray;
    private double minYears = Double.MAX_VALUE;
    private double maxYears = 0;

    public KmeansShakedImprovement(Processing processing)
    {
        this.processing = processing;
    }

    public void runAlgorithm() {
        p_mArray = processing.p_m(processing.getSelectedMoviesIds());
        processing.getSelectedMoviesIds().forEach(x->calculateProbs(x, processing.getSelectedMoviesIds(), p_mArray));
        createVectors();
        KMeans(10);
    }

    public void calculateProbs(int movieID_i, List<Integer> selectedMoviesIds, double[] p_mArray) {
        for (int movieID_j : selectedMoviesIds) {
            if (movieID_i < movieID_j) {
                Set<Integer> temp = new HashSet<>(processing.getMovieIdToUsersIds().get(movieID_i));
                temp.retainAll(processing.getMovieIdToUsersIds().get(movieID_j));

                double yearMovieI = processing.getMovieIdToMovieProps().get(movieID_i).getThird();


                if (yearMovieI > maxYears) {
                    maxYears = yearMovieI;
                }
                if (yearMovieI < minYears) {
                    minYears = yearMovieI;
                }

                double sum = 0;
                for (int userID : temp) {
                    double n = processing.getUserIdToMoviesIds().get(userID).size();
                    sum = sum + (2 / (n * (n - 1)));
                }
                double sharedP = (1.0 / (processing.getNumberOfUsers() + 1)) *
                        ((2.0 / (processing.getNumberOfMovies() * (processing.getNumberOfMovies() - 1))) + sum);

                sharedP = Math.log(sharedP);

                double firstP = Math.log(p_mArray[selectedMoviesIds.indexOf(movieID_i)]);
                double secondP = Math.log(p_mArray[selectedMoviesIds.indexOf(movieID_j)]);
                if ((firstP + secondP) <= sharedP)
                {
                    if (correlation.containsKey(movieID_i)) {
                        correlation.get(movieID_i).add(movieID_j);
                    } else {
                        ArrayList<Integer> list = new ArrayList<>();
                        list.add(movieID_j);
                        correlation.put(movieID_i, list);
                    }
                    if (correlation.containsKey(movieID_j)) {
                        correlation.get(movieID_j).add(movieID_i);
                    } else {
                        ArrayList<Integer> list = new ArrayList<>();
                        list.add(movieID_i);
                        correlation.put(movieID_j, list);
                    }
                    //}

                }
                AbstractMap.SimpleEntry<Integer, Integer> toInsertPair= new AbstractMap.SimpleEntry<Integer, Integer>(movieID_i,movieID_j);
                RelationFraction.put(toInsertPair, sharedP);
            }
        }
    }

    public void createVectors()
    {
        for (Integer entry : processing.getSelectedMoviesIds())
        {
            double[] vector = new double[1];
            vector[0] =  Math.log(p_mArray[processing.getSelectedMoviesIds().indexOf(entry)]);
            movieVectors.put(entry, vector);
            vectorMovies.put(vector, entry);
        }
    }

    public ArrayList<double[]> generateRandomCentroids(int numberOfClusters)
    {
        ArrayList<double[]> centroids = new ArrayList<>();
        int i = 0;
        while (i < numberOfClusters) {
            int index = (int) (Math.random() * processing.getSelectedMoviesIds().size());
            int movie = processing.getSelectedMoviesIds().get(index);
            double[] randomVector = null;
            if (this.movieVectors.containsKey(movie)) {
                randomVector = this.movieVectors.get(movie);
                centroids.add(randomVector);
                clusterToMovie.put(i, movie);
                this.movieVectors.remove(movie);
                i++;
            }
        }

        return centroids;
    }

    public double calculateDistance(double[] vector1, double[] vector2)
    {
        Integer movie1 = vectorMovies.get(vector1);
        Integer movie2 = vectorMovies.get(vector2);
        System.out.println(movie1);
        System.out.println(movie2);
        if (movie1 == null || movie2 == null)
        {
            return Math.pow((vector1[0] * vector2[0]) - Math.log(0.5), 2);
        }
        if (movie1 < movie2)
        {
            return Math.pow((vector1[0] * vector2[0]) - Math.log(RelationFraction.get(new AbstractMap.SimpleEntry<>(movie1, movie2))), 2);
        }
        else
        {
            return Math.pow((vector1[0] * vector2[0]) - Math.log(RelationFraction.get(new AbstractMap.SimpleEntry<>(movie2, movie1))), 2);
        }
    }

    public double[] calculateNewCentroid(double[] centroid, HashSet<Integer> movies)
    {
        double[] newCentroid = new double[centroid.length];
        if (movies.size() > 0)
        {
            for (int i = 0 ; i < centroid.length ; i++) {
                double sum = 0;
                for (Integer movie : movies) {
                    sum = sum + this.movieVectors.get(movie)[i];
                }
                newCentroid[i] = sum / movies.size();
            }
            return newCentroid;
        }
        else
        {
            return centroid;
        }
    }

    public void KMeans(int K)
    {

        ArrayList<double[]> centroids = generateRandomCentroids(K);
        HashMap<Integer, HashSet<Integer>> cetroidsGroups = new HashMap<>();
        for (int j = 0 ; j < 10 ; j++)
        {
            cetroidsGroups = new HashMap<>();
            for (int i = 0; i < K; i++)
            {
                cetroidsGroups.put(i, new HashSet<Integer>());
            }
            for (Integer movie : processing.getSelectedMoviesIds())
            {
                if (this.movieVectors.containsKey(movie)) {
                    double minDist = Double.MAX_VALUE;
                    int minIndexOfCentroids = -1;
                    for (int l = 0; l < K; l++){
                        double dist = calculateDistance(this.movieVectors.get(movie), centroids.get(l));
                        if (dist <= minDist) {
                            minIndexOfCentroids = l;
                            minDist = dist;
                        }
                    }
                    cetroidsGroups.get(minIndexOfCentroids).add(movie);
                }

                HashMap<Integer, HashSet<Integer>> cetroidsGroupsNew = new HashMap<>();
                for (Integer entry : cetroidsGroupsNew.keySet())
                {
                    HashSet<Integer> h = new HashSet<>(cetroidsGroups.get(entry));
                    cetroidsGroupsNew.put(entry, h);
                }
            }

            ArrayList<double[]> newCentroids = new ArrayList<>();
            for (int i = 0; i < K; i++)
            {
                newCentroids.add(calculateNewCentroid(centroids.get(i), cetroidsGroups.get(i)));
            }
            centroids = new  ArrayList<double[]>(newCentroids);
        }
        double sumClustering = 0;
        for (int h = 0 ; h < centroids.size() ; h++)
        {
            HashSet<Integer> cluster = new HashSet<Integer>(cetroidsGroups.get(h));
            cluster.add(clusterToMovie.get(h));
            for (int movieId : cluster)
            {
                System.out.print(movieId + " " + processing.getMovieIdToMovieProps().get(movieId).getFirst() + ",");
            }
            System.out.println();
            sumClustering= sumClustering + claculateCost(cluster, p_mArray , processing.getSelectedMoviesIds(), RelationFraction);
        }
        System.out.println(sumClustering);
    }

    public static double claculateCost(HashSet<Integer> cluster, double[] p_mArray ,  List<Integer> selectedMoviesIds,
                                       HashMap<AbstractMap.SimpleEntry<Integer, Integer>, Double> RelationFraction)
    {
        if (cluster.size() == 1)
        {
            return Math.log(1.0 / p_mArray[selectedMoviesIds.indexOf((int)cluster.toArray()[0])]);
        }

        else
        {
            double coeff = 1.0 / (cluster.size() - 1);
            double cost = 0.0;
            for (int movieId_i : cluster)
            {
                for (int movieId_j : cluster)
                {
                    if (movieId_i < movieId_j)
                    {
                        cost = cost + Math.log(1.0 /
                                Math.exp(RelationFraction.get(new AbstractMap.SimpleEntry<Integer, Integer>(movieId_i, movieId_j))));
                    }
                }
            }
            return cost * coeff;
        }
    }
}
