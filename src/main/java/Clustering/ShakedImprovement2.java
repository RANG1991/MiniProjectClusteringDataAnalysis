package Clustering;

import java.util.*;

public class ShakedImprovement2 {
    private Processing processing;
    private HashMap<Integer, double[]> movieVectors = new HashMap<>();
    private  HashMap<Integer, ArrayList<Integer>> correlation = new HashMap<>();
    private HashMap<AbstractMap.SimpleEntry<Integer, Integer>, Double> RelationFraction = new HashMap<>();
    private HashMap<Integer, Integer> clusterToMovie = new HashMap<>();
    private double[] p_mArray;
    private double minYears = Double.MAX_VALUE;
    private double maxYears = 0;

    public ShakedImprovement2(Processing processing)
    {
        this.processing = processing;
    }

    public void runAlgorithm() {
        p_mArray = processing.p_m(processing.getSelectedMoviesIds());
        processing.getSelectedMoviesIds().forEach(x->calculateProbs(x, processing.getSelectedMoviesIds(), p_mArray));
        createVectors();
        KMedoids(70);
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
            for (int i = 0 ; i < processing.getSelectedMoviesIds().size(); i++)
            {
                vector[0] = this.p_mArray[i];
            }
            movieVectors.put(entry, vector);
        }
    }

    public ArrayList<Integer> generateRandomCentroids(int numberOfClusters)
    {
        ArrayList<Integer> medoids = new ArrayList<>();
        int i = 0;
        while (i < numberOfClusters) {
            int index = (int) (Math.random() * processing.getSelectedMoviesIds().size());
            int movie = processing.getSelectedMoviesIds().get(index);
            if (!medoids.contains(movie))
            {
                medoids.add(movie);
                i++;
            }
        }

        return medoids;
    }

    public double calculateDistance(Integer movie1, Integer movie2)
    {
        if (movie2 < movie1)
        {
            Integer temp = movie1;
            movie1 = movie2;
            movie2 = temp;
        }
        return (this.movieVectors.get(movie1)[0] * this.movieVectors.get(movie2)[0]) - this.RelationFraction.get(new AbstractMap.SimpleEntry<>(movie1, movie2));
    }

    public double calculateDistance2(Integer movie1, HashSet<Integer> group)
    {
        double dist = 0;
        for (Integer movie2 : group) {
            if (movie2 < movie1) {
                Integer temp = movie1;
                movie1 = movie2;
                movie2 = temp;
            }
            dist = dist + ((Math.log(this.movieVectors.get(movie1)[0]) + Math.log(this.movieVectors.get(movie2)[0])) - this.RelationFraction.get(new AbstractMap.SimpleEntry<>(movie1, movie2)));
        }
        return dist / group.size();
    }


    public void KMedoids(int K)
    {
        ArrayList<Integer> medoids = generateRandomCentroids(K);
        System.out.println(medoids);
        HashMap<Integer, HashSet<Integer>> finalMediodsGroups = new HashMap<Integer, HashSet<Integer>>();
        finalMediodsGroups = createClusters2(medoids);
        double initialSum = sumClustering(finalMediodsGroups);
        for (int j  = 0 ; j < 10 ; j++) {
            for (int i = 0; i < medoids.size(); i++) {
                Integer medoid = medoids.get(i);
                for (Integer movie : processing.getSelectedMoviesIds()) {
                    if (!medoids.contains(movie)) {
                        medoids.set(i, movie);
                        HashMap<Integer, HashSet<Integer>> tempMediodsGroups = createClusters2(medoids);
                        double currSum = sumClustering(finalMediodsGroups);
                        if (currSum >= 0 ) {
                            if (currSum < initialSum) {
                                finalMediodsGroups = tempMediodsGroups;
                                System.out.println(currSum);
                                initialSum = currSum;
                            } else {
                                medoids.set(i, medoid);
                            }
                        }
                        else {
                            medoids.set(i, medoid);
                        }
                    }
                }
            }
        }
        //add for hierarcial
        System.out.println("before: " + initialSum);
        Hierarcial(finalMediodsGroups);
    }


    public double Hierarcial(HashMap<Integer, HashSet<Integer>> medoidsGroups)
    {
        HashMap<Integer, HashSet<Integer>> finalMedoidsGroups = null;
        double initialSum = sumClustering(medoidsGroups);
        System.out.println("after: " + initialSum);
        for (int j  = 0 ; j < 60 ; j++) {
            double currSum = createClustersHierarcial(medoidsGroups);
            if (initialSum > currSum)
            {
                System.out.println(currSum);
                initialSum = currSum;
                finalMedoidsGroups = new HashMap<>(medoidsGroups);
            }
        }
        return initialSum;
    }


    public double calculateDistanceHierarcial(HashSet<Integer> group1, HashSet<Integer> group2) {
        double dist = 0;
        for (Integer movie1 : group1) {
            for (Integer movie2 : group2) {
                if (movie2 < movie1) {
                    Integer temp = movie1;
                    movie1 = movie2;
                    movie2 = temp;
                }
                dist = dist + ((this.movieVectors.get(movie1)[0] * this.movieVectors.get(movie2)[0]) - this.RelationFraction.get(new AbstractMap.SimpleEntry<>(movie1, movie2)));
            }
        }
        double size = group1.size() * group2.size();
        return dist / size;
    }

    public double createClustersHierarcial(HashMap<Integer, HashSet<Integer>> medoidsGroups)
    {

        double minDist = Integer.MAX_VALUE;
        int minMedoid1 = -1;
        int minMedoid2 = -1;
        for (Integer medoid1 : medoidsGroups.keySet()) {
            for (Integer medoid2 : medoidsGroups.keySet()) {
                if (medoid1 < medoid2) {
                    double dist = calculateDistanceHierarcial(medoidsGroups.get(medoid1), medoidsGroups.get(medoid2));
                    if (dist <= minDist) {
                        minMedoid1 = medoid1;
                        minMedoid2 = medoid2;
                        minDist = dist;
                    }
                }
            }
        }
        if (minMedoid1 !=-1 && minMedoid2!=-1){
            Integer minMedoid = Math.min(minMedoid1, minMedoid2);
            Integer maxMedoid = Math.max(minMedoid1, minMedoid2);
            HashSet<Integer> clusterOfMax = medoidsGroups.get(maxMedoid);
            medoidsGroups.get(minMedoid).addAll(clusterOfMax);
            medoidsGroups.remove(maxMedoid);
        }

        return sumClustering(medoidsGroups);
    }


    public HashMap<Integer, HashSet<Integer>> createClusters(ArrayList<Integer> medoids)
    {
        HashMap<Integer, HashSet<Integer>> medoidsGroups = new HashMap<>();

        for (int i = 0; i < medoids.size(); i++)
        {
            medoidsGroups.put(medoids.get(i), new HashSet<Integer>());
        }
        for (Integer movie : processing.getSelectedMoviesIds()) {
            if (!medoids.contains(movie)) {
                int minMedoid = -1;
                double minDist = Double.MAX_VALUE;
                for (Integer medoid : medoidsGroups.keySet()) {
                            double dist = calculateDistance(movie, medoid);
                            if (dist <= minDist) {
                                minMedoid = medoid;
                                minDist = dist;
                        }
                }
                medoidsGroups.get(minMedoid).add(movie);
            }
        }
        return medoidsGroups;
    }

    public HashMap<Integer, HashSet<Integer>> createClusters2(ArrayList<Integer> medoids)
    {
        HashMap<Integer, HashSet<Integer>> medoidsGroups = new HashMap<>();

        for (int i = 0; i < medoids.size(); i++)
        {
            medoidsGroups.put(medoids.get(i), new HashSet<Integer>());
            medoidsGroups.get(medoids.get(i)).add(medoids.get(i));
        }
        for (Integer movie : processing.getSelectedMoviesIds()) {
            if (!medoids.contains(movie)) {
                int minMedoid = -1;
                double minDist = Double.MAX_VALUE;
                for (Integer medoid : medoidsGroups.keySet()) {
                    double dist = calculateDistance2(movie, medoidsGroups.get(medoid));
                    if (dist <= minDist) {
                        minMedoid = medoid;
                        minDist = dist;
                    }
                }
                medoidsGroups.get(minMedoid).add(movie);
            }
        }
        return medoidsGroups;
    }

    public double sumClustering(HashMap<Integer, HashSet<Integer>> medoidsGroups)
    {
        double sumClustering = 0;
        for (Integer medoid : medoidsGroups.keySet())
        {
            HashSet<Integer> cluster = new HashSet<Integer>(medoidsGroups.get(medoid));
            cluster.add(medoid);
            for (int movieId : cluster)
            {
                //   System.out.print(movieId + " " + processing.getMovieIdToMovieProps().get(movieId).getFirst() + ",");
            }
            //System.out.println();
            sumClustering= sumClustering + claculateCost(cluster, p_mArray , processing.getSelectedMoviesIds(), RelationFraction);
        }
        return sumClustering;
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

