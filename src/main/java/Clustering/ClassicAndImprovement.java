package Clustering;

import java.util.*;

public class ClassicAndImprovement {
    private Processing processing;
    private HashMap<Integer, double[]> movieVectors = new HashMap<>();
    private  HashMap<Integer, ArrayList<Integer>> correlation = new HashMap<>();
    private HashMap<AbstractMap.SimpleEntry<Integer, Integer>, Double> RelationFraction = new HashMap<>();
    private HashMap<Integer, Integer> clusterToMovie = new HashMap<>();
    private double[] p_mArray;
    private double minYears = Double.MAX_VALUE;
    private double maxYears = 0;

    public ClassicAndImprovement(Processing processing)
    {
        this.processing = processing;
    }

    public void runAlgorithm() {
        p_mArray = processing.p_m(processing.getSelectedMoviesIds());
        processing.getSelectedMoviesIds().forEach(x->calculateProbs(x, processing.getSelectedMoviesIds(), p_mArray));
        createVectors();
        correlationAlgorithm(processing.getSelectedMoviesIds(),correlation);
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

    public void correlationAlgorithm(List<Integer> selectedMoviesIds, HashMap<Integer, ArrayList<Integer>> correlation)
    {
        HashMap<Integer, HashSet<Integer>> clustering = new HashMap<Integer, HashSet<Integer>>();
        ArrayList<Integer> remaining = new ArrayList<Integer>(selectedMoviesIds);
        HashSet<Integer> clusteringSet = new HashSet<>();
        int sum = 0;
        while (remaining.size() > 0)
        {
            HashSet<Integer> cluster = new HashSet<Integer>();
            int index = (int) Math.floor(Math.random() * remaining.size());
            cluster = realCluster(correlation.get(remaining.get(index)));

            cluster.add(remaining.get(index));
            cluster.removeAll(clusteringSet);
            clusteringSet.addAll(cluster);
            clustering.put(remaining.get(index), cluster);
            remaining.removeAll(cluster);
            sum = sum + cluster.size();
        }

      //  Hierarcial(clustering);
        System.out.println(sumClustering(finalStage(clustering)));
    }

    public double calculateSingleIterCost(HashMap<Integer, HashSet<Integer>> clustering)
    {
        double sumClustering = 0;
        for (Integer movieID : clustering.keySet())
        {
            HashSet<Integer> cluster = new HashSet<Integer>(clustering.get(movieID));
            sumClustering= sumClustering + claculateCost(cluster, p_mArray , processing.getSelectedMoviesIds(), RelationFraction);
        }
        return sumClustering;
    }

    public HashMap<Integer, HashSet<Integer>> finalStage(HashMap<Integer, HashSet<Integer>> clustering) {

        Integer from_head1 = -1;
        Integer to_head2 = -1;
        Integer which_movie = -1;



        HashMap<Integer, HashSet<Integer>> tempClustering = new HashMap<>();
        HashMap<Integer, HashSet<Integer>> finalClustering = new HashMap<>();

        int size = 0;
        for (Integer movie : clustering.keySet())
        {
            size += clustering.get(movie).size();
        }
        System.out.println(size);

        boolean canImprove = true;

        System.out.println("start func " + clustering);

        for (Integer entry : clustering.keySet()) {
            HashSet<Integer> h = new HashSet<>(clustering.get(entry));
            tempClustering.put(entry, h);
        }
        int count = 0;
        double initalSum = calculateSingleIterCost(clustering);
        double initalSumFinal = initalSum;
        while (canImprove)
        {
            for (Integer movie_head : clustering.keySet()) {
                for (Integer movie : clustering.get(movie_head)) {


                    tempClustering.get(movie_head).remove(movie);
                    if (movie == movie_head)
                    {
                        if (!tempClustering.get(movie_head).isEmpty()) {
                            tempClustering.put(tempClustering.get(movie_head).iterator().next(), tempClustering.get(movie_head));
                        }
                    }
                    HashSet<Integer> cluster_temp = new HashSet<>();
                    cluster_temp.add(movie);
                    tempClustering.put(movie, cluster_temp);


                    double currSum = calculateSingleIterCost(tempClustering);
                    if (initalSum > currSum) {
                        initalSum = currSum;
                        System.out.println(initalSum);
                        from_head1 = movie_head;
                        to_head2 = -2;
                        which_movie = movie;

                        size = 0;
                        for (Integer movieForSize : tempClustering.keySet()) {
                            size += tempClustering.get(movieForSize).size();
                        }
                        System.out.println("size after end iter: " + size);
                    }

                    for (Integer entry : clustering.keySet()) {
                        HashSet<Integer> h = new HashSet<>(clustering.get(entry));
                        tempClustering.put(entry, h);
                    }


                    for (Integer movie_head_2 : clustering.keySet()) {
                        if (movie_head_2 != movie_head) {
                            tempClustering.get(movie_head).remove(movie);
                            tempClustering.get(movie_head_2).add(movie);

                            if (movie_head == movie) {

                                if (!tempClustering.get(movie_head).isEmpty()) {
                                    tempClustering.put(tempClustering.get(movie_head).iterator().next(), tempClustering.get(movie_head));

                                }
                                tempClustering.remove(movie_head);
                            }

                            if (count == 3) {
                                System.out.println("in iter  " + tempClustering);

                            }

                            currSum = calculateSingleIterCost(tempClustering);
                            if (initalSum > currSum) {
                                initalSum = currSum;
                                System.out.println(initalSum);
                                from_head1 = movie_head;
                                to_head2 = movie_head_2;
                                which_movie = movie;

                                size = 0;
                                for (Integer movieForSize : tempClustering.keySet()) {
                                    size += tempClustering.get(movieForSize).size();
                                }
                                System.out.println("size after end iter: " + size);
                            }

                            for (Integer entry : clustering.keySet()) {
                                HashSet<Integer> h = new HashSet<>(clustering.get(entry));
                                tempClustering.put(entry, h);
                            }

                        }
                    }
                }
            }
            //after for

            if (initalSum >= initalSumFinal)
            {
                canImprove = false;
            }
            else
            {
                if (from_head1!=-1 && to_head2!=-1) {

                    if (to_head2 == -2) {
                        tempClustering.get(from_head1).remove(which_movie);
                        if (which_movie == from_head1) {
                            if (!tempClustering.get(from_head1).isEmpty()) {
                                tempClustering.put(tempClustering.get(from_head1).iterator().next(), tempClustering.get(from_head1));
                            }
                        }
                        HashSet<Integer> cluster_temp = new HashSet<>();
                        cluster_temp.add(which_movie);
                        tempClustering.put(which_movie, cluster_temp);
                    } else {
                        tempClustering.get(from_head1).remove(which_movie);
                        tempClustering.get(to_head2).add(which_movie);
                        if (from_head1 == which_movie) {
                            if (!tempClustering.get(from_head1).isEmpty()) {
                                tempClustering.put(tempClustering.get(from_head1).iterator().next(), tempClustering.get(from_head1));

                            }
                            tempClustering.remove(from_head1);
                        }
                    }
                    for (Integer entry : tempClustering.keySet()) {
                        HashSet<Integer> h = new HashSet<>(tempClustering.get(entry));
                        clustering.put(entry, h);
                    }

                }
                initalSumFinal = initalSum;
                count++;
            }
        }

        size = 0;


        finalClustering = new HashMap<>();
        for (Integer entry : clustering.keySet()) {
            HashSet<Integer> h = new HashSet<>(clustering.get(entry));
            finalClustering.put(entry, h);
        }
        for (Integer movie : finalClustering.keySet())
        {
            size += finalClustering.get(movie).size();
        }
        System.out.println(size);

        return finalClustering;
    }





    private HashSet<Integer> realCluster(ArrayList<Integer> remaning) {
        ArrayList<Integer> toRemove = new ArrayList<Integer>();
        HashSet<Integer> cluster = new HashSet<Integer>(remaning);
        for(int movieId_i : remaning){
            int size = remaning.size();
            int counter=0;
            for (int movieId_j : remaning)
            {
                if ((movieId_i != movieId_j) && !toRemove.contains(movieId_i)){
                    if (correlation.get(movieId_i).contains(movieId_j)){
                        counter++;
                    }
                }
            }
            if (counter < size/(2)){
                toRemove.add(movieId_i);
            }
        }
        cluster.removeAll(toRemove);
        return cluster;

    }



    public double Hierarcial(HashMap<Integer, HashSet<Integer>> medoidsGroups)
    {
        System.out.println(medoidsGroups);
        HashMap<Integer, HashSet<Integer>> finalMedoidsGroups = null;
        double initialSum = sumClustering(medoidsGroups);
        System.out.println("after: " + initialSum);
        for (int j  = 0 ; j < 5 ; j++) {
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
                dist = dist + this.RelationFraction.get(new AbstractMap.SimpleEntry<>(movie1, movie2)) - ((Math.log(this.movieVectors.get(movie1)[0]) + Math.log(this.movieVectors.get(movie2)[0])));
                //System.out.println(dist);
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
                    double dist= calculateDistanceHierarcial(medoidsGroups.get(medoid1), medoidsGroups.get(medoid2));
                       if (dist>=0 && dist <= minDist){

                               minMedoid1 = medoid1;
                               minMedoid2 = medoid2;
                               minDist = dist;

                       }


                }
            }
        }
        if(minMedoid1 !=-1 && minMedoid2!=-1){
            Integer minMedoid = Math.min(minMedoid1, minMedoid2);
            Integer maxMedoid = Math.max(minMedoid1, minMedoid2);
            HashSet<Integer> clusterOfMax = medoidsGroups.get(maxMedoid);
            medoidsGroups.get(minMedoid).addAll(clusterOfMax);
            medoidsGroups.remove(maxMedoid);
        }

        return sumClustering(medoidsGroups);
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
