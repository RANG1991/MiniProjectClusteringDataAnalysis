package Clustering;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShakedImprovement {
	private Processing processing;
    private HashMap<Integer, double[]> movieVectors = new HashMap<>();
    private HashMap<Integer, ArrayList<Integer>> correlation = new HashMap<>();
    private HashMap<AbstractMap.SimpleEntry<Integer, Integer>, Double> RelationFraction = new HashMap<>();
    private HashMap<AbstractMap.SimpleEntry<Integer, Integer>, Double> matrixDistance = new HashMap<>();
    private double[] p_mArray;
    private double minYears = Double.MAX_VALUE;
    private double maxYears = 0;

    public ShakedImprovement(Processing processing)
    {
        this.processing = processing;
    }

    public void runAlgorithm() {
        p_mArray = processing.p_m(processing.getSelectedMoviesIds());
        processing.getSelectedMoviesIds().forEach(x->calculateProbs(x, processing.getSelectedMoviesIds(), p_mArray));
        createVectors();
        for (Integer movie : processing.getSelectedMoviesIds())
        {
        	for (Integer movie2 : processing.getSelectedMoviesIds())
        	{
        		if (movie <= movie2)
        		{
        			matrixDistance.put(new AbstractMap.SimpleEntry<Integer, Integer>(movie, movie2), calculateDistance(movieVectors.get(movie),
        					movieVectors.get(movie2)));
        		}
        	}
        }
        for (AbstractMap.SimpleEntry<Integer, Integer> movies : this.matrixDistance.keySet())
        {
        	System.out.println(this.matrixDistance.get(movies));
        }
        ArrayList<Integer> remaining = new ArrayList<Integer>(this.processing.getSelectedMoviesIds());
    	ArrayList<HashSet<Integer>> clustering = new ArrayList<>();
    	while (remaining.size() >= 2)
    	{
    		calculateSingleIteration(remaining, calculateMean(remaining), clustering);
    	}
    	double sumClustering = 0;
        for (HashSet<Integer> cluster : clustering)
        {
            for (int movieId : cluster)
            {
                System.out.print(movieId + " " + processing.getMovieIdToMovieProps().get(movieId).getFirst() + ",");
            }
            System.out.println();
            sumClustering= sumClustering + claculateCost(cluster, p_mArray , processing.getSelectedMoviesIds(), RelationFraction);
        }
        System.out.println(sumClustering);
    }
    
    public void calculateSingleIteration(ArrayList<Integer> remaining, double mean, ArrayList<HashSet<Integer>> clustering)
    {
        int index = (int) Math.floor(Math.random() * remaining.size());
        Integer movie = remaining.get(index);
        remaining.remove(movie);
        int index2 = (int) Math.floor(Math.random() * remaining.size());
        Integer movie2 = remaining.get(index2);
        remaining.remove(movie2);
        HashSet<Integer> cluster = new HashSet<Integer>();
        cluster.add(movie);
        cluster.add(movie2);
        HashSet<Integer> toRemove = new HashSet<Integer>();
        for (int i = 0 ; i < remaining.size(); i++)
    	{
        	double distance = 0.0;
			for (Integer movieInCluster : cluster)
			{
				if (remaining.get(i) <= movieInCluster)
				{
					distance = distance + matrixDistance.get(new AbstractMap.SimpleEntry<>(remaining.get(i), movieInCluster));
				}
				else
				{
					distance = distance + matrixDistance.get(new AbstractMap.SimpleEntry<>(movieInCluster, remaining.get(i)));
				}
			}
			if (distance / cluster.size() <= mean / 1.5)
			{
				cluster.add(remaining.get(i));
				toRemove.add(remaining.get(i));
			}
    	}
        remaining.removeAll(toRemove);
        clustering.add(cluster);
    }
    
    public double calculateMean(ArrayList<Integer> remaining)
    {
    	double sumOfDistances = 0.0;
        for (Integer movie : remaining)
        {
        	for (Integer movie2 : remaining)
        	{
        		if (movie <= movie2)
        		{
        			sumOfDistances = sumOfDistances + matrixDistance.get(new AbstractMap.SimpleEntry<>(movie, movie2));
        		}
        	}
        }
        return sumOfDistances / ((remaining.size() * remaining.size() + 1) / 2);
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
            double[] vector = new double[processing.getNumberOfUsers() + processing.getAllGenres().size() + 1];
            for (int i=0 ; i < processing.getAllGenres().size() ; i++)
            {
                if (processing.getMovieIdToMovieProps().get(entry).getSecond().contains(processing.getAllGenres().get(i))) {
                    vector[i] = 1.0;
                }
                else
                {
                    vector[i] = 0.0;
                }
            }
            for (int i = processing.getAllGenres().size() - 1 ; i < processing.getNumberOfUsers() + processing.getAllGenres().size() ; i++)
            {
                if (processing.getMovieIdToUsersIdsIncludingRatings().get(entry).containsKey(i+1 - processing.getAllGenres().size()))
                {
                    vector[i+1] = Double.valueOf(processing.getMovieIdToUsersIdsIncludingRatings().get(entry).get(i+1 - processing.getAllGenres().size()));
                }
                else
                {
                    vector[i+1] = 0.0;
                }
            }
            vector[vector.length - 1] = (Double.valueOf(processing.getMovieIdToMovieProps().get(entry).getThird()) - this.minYears) / (maxYears - minYears);
            movieVectors.put(entry, vector);
        }
    }

    public double calculateDistance(double[] vector1, double[] vector2)
    {
        double sum = 0;
        for (int i = 0 ; i < vector2.length ; i++)
        {
           sum = sum + Math.pow(vector1[i] - vector2[i], 2);
        }
        return Math.sqrt(sum);
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
