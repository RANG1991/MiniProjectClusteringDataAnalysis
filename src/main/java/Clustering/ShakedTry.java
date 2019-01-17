package Clustering;

import java.util.*;

public class ShakedTry {
    private Processing processing;
    private HashMap<Integer, double[]> movieVectors = new HashMap<>();
    private  HashMap<Integer, ArrayList<Integer>> correlation = new HashMap<>();
    private HashMap<AbstractMap.SimpleEntry<Integer, Integer>, Double> RelationFraction = new HashMap<>();
    private HashMap<Integer, Integer> clusterToMovie = new HashMap<>();
    private double[] p_mArray;
    private double minYears = Double.MAX_VALUE;
    private double maxYears = 0;

    public ShakedTry(Processing processing)
    {
        this.processing = processing;
    }

    public void runAlgorithm() {
        p_mArray = processing.p_m(processing.getSelectedMoviesIds());
        processing.getSelectedMoviesIds().forEach(x->calculateProbs(x, processing.getSelectedMoviesIds(), p_mArray));
        createVectors();
        System.out.println(KMedoids());
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


    public double calculateDistance(HashSet<Integer> group1, HashSet<Integer> group2) {
        double dist = 0;
        for (Integer movie1 : group1) {
            for (Integer movie2 : group2) {
                if (movie2 < movie1) {
                    Integer temp = movie1;
                    movie1 = movie2;
                    movie2 = temp;
                }
                dist = dist + ((Math.log(this.movieVectors.get(movie1)[0]) + Math.log(this.movieVectors.get(movie2)[0])) - this.RelationFraction.get(new AbstractMap.SimpleEntry<>(movie1, movie2)));
            }
        }
        double size = group1.size() * group2.size();
        return dist / size;
    }

    public double calculateSingleIterCost(HashMap<Integer, HashSet<Integer>> medoidsGroups)
    {
        double sumClustering = 0;
        for (Integer medoid : medoidsGroups.keySet())
        {
            HashSet<Integer> cluster = new HashSet<Integer>(medoidsGroups.get(medoid));
            // cluster.add(medoids.get(h));
            for (int movieId : cluster)
            {
                //   System.out.print(movieId + " " + processing.getMovieIdToMovieProps().get(movieId).getFirst() + ",");
            }
            //System.out.println();
            sumClustering= sumClustering + claculateCost(cluster, p_mArray , processing.getSelectedMoviesIds(), RelationFraction);
        }
        return sumClustering;
    }


    public double KMedoids()
    {
        ArrayList<Integer> medoids = generateRandomCentroids(processing.getNumberOfSelctedMovies());
        HashMap<Integer, HashSet<Integer>> medoidsGroups = new HashMap<>();
        HashMap<Integer, HashSet<Integer>> finalMedoidsGroups = null;
        boolean canImprove = true;

        for (int i = 0; i < medoids.size(); i++)
        {
            medoidsGroups.put(medoids.get(i), new HashSet<Integer>());
            medoidsGroups.get(medoids.get(i)).add(medoids.get(i));
        }
        System.out.println(medoidsGroups);
        double initalSum = calculateSingleIterCost(medoidsGroups);
        while(canImprove) {
            if (medoidsGroups.size()> 1){
                double currSum = createClusters(medoidsGroups);
                if (currSum < initalSum )
                {
                    System.out.println(currSum);
                    initalSum = currSum;
                    finalMedoidsGroups = new HashMap<>(medoidsGroups);
                }
                else {
                        canImprove = false;
                }
            }
        }
        return initalSum;
    }



    public double createClusters(HashMap<Integer, HashSet<Integer>> medoidsGroups)
    {
//        int pri=0;
//        //System.out.println("start: " + medoidsGroups);
//        if (pri==0){
//            System.out.println("start: " + medoidsGroups);
//        }

        HashMap<Integer, HashSet<Integer>> tempMedoidsGroups = new HashMap<>();

        for(Integer entry : medoidsGroups.keySet() ){
            HashSet<Integer> h = new HashSet<> (medoidsGroups.get(entry));
            tempMedoidsGroups.put(entry,h);
        }


        double minDist = Integer.MAX_VALUE;
        int minMedoid1 = -1;
        int minMedoid2 = -1;

        for (Integer medoid1 : medoidsGroups.keySet()) {
            for (Integer medoid2 : medoidsGroups.keySet()) {
                if (medoid1 < medoid2) {
                    HashSet<Integer> clusterOfMax = tempMedoidsGroups.get(medoid2);
                    tempMedoidsGroups.get(medoid1).addAll(clusterOfMax);
                    tempMedoidsGroups.remove(medoid2);
                    //System.out.println("temp: "+  tempMedoidsGroups);
//                    if (pri==0 || pri==2){
//                        System.out.println("temp: "+  tempMedoidsGroups);
//                        if (pri==2){pri=1;}
//                    }

                    double currSum = calculateSingleIterCost(tempMedoidsGroups);
                    if (currSum <= minDist) {
                        minMedoid1 = medoid1;
                        minMedoid2 = medoid2;
                        minDist = currSum;
                    }
                    for(Integer entry : medoidsGroups.keySet() ){
                        HashSet<Integer> h = new HashSet<> (medoidsGroups.get(entry));
                        tempMedoidsGroups.put(entry,h);
                    }
//                    if (pri==0){
//                        System.out.println("middle: " + tempMedoidsGroups);
//                        pri=2;
//                    }
                   // System.out.println("middle: " + tempMedoidsGroups);

                }
            }
        }
        if (minMedoid1!=-1 && minMedoid2!=-1){
            HashSet<Integer> clusterOfMax = medoidsGroups.get(minMedoid2);
            medoidsGroups.get(minMedoid1).addAll(clusterOfMax);
            medoidsGroups.remove(minMedoid2);
        }
       // System.out.println("end: " + medoidsGroups);

        return calculateSingleIterCost(medoidsGroups);
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

