package Clustering;

import java.util.*;

public class CorrelationAlgorithmImprovement {
	private Processing processing;
	private HashMap<Integer, double[]> movieVectors = new HashMap<>();
	private  HashMap<Integer, ArrayList<Integer>> correlation = new HashMap<>();
	private HashMap<AbstractMap.SimpleEntry<Integer, Integer>, Double> RelationFraction = new HashMap<>();
	private double[] p_mArray;
	private double minYears = Double.MAX_VALUE;
	private double maxYears = 0;

	public CorrelationAlgorithmImprovement(Processing processing)
	{
		this.processing = processing;
	}

	public double runAlgorithm() {
		p_mArray = processing.p_m(processing.getSelectedMoviesIds());
		processing.getSelectedMoviesIds().forEach(x->calculateProbs(x, processing.getSelectedMoviesIds(), p_mArray));
		createVectors();
		return runClustering();
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
			double[] vector = new double[1];
			for (int i = 0 ; i < processing.getSelectedMoviesIds().size(); i++)
			{
				vector[0] = this.p_mArray[i];
			}
			movieVectors.put(entry, vector);
		}
	}

	public double calculateSingleIterCost(HashMap<Integer, HashSet<Integer>> clustering)
	{
		double sumClustering = 0;
		for (Integer movieID : clustering.keySet())
		{
			HashSet<Integer> cluster = new HashSet<Integer>(clustering.get(movieID));
			sumClustering= sumClustering + calculateCost(cluster, p_mArray , processing.getSelectedMoviesIds(), RelationFraction);
		}
		return sumClustering;
	}


	public double runClustering()
	{
		ArrayList<Integer> remaining = new ArrayList<Integer>(processing.getSelectedMoviesIds());
		HashMap<Integer, HashSet<Integer>> clustering = new HashMap<>();
		HashSet<Integer> clusteringSet = new HashSet<>();
		int sum = 0;
		while (remaining.size() > 0)
		{

			int minUsers = Integer.MAX_VALUE;
			int indexOfMin = 0;
			for (int i = 0 ; i < remaining.size() ; i++)
			{
				if (minUsers > processing.getMovieIdToUsersIds().get(remaining.get(i)).size())
				{
					minUsers = processing.getMovieIdToUsersIds().get(remaining.get(i)).size();
					indexOfMin = i;
				}
			}
			int index = indexOfMin;

            HashSet<Integer> cluster = new HashSet<>();
            if (correlation.containsKey(remaining.get(index))) {
                cluster = realCluster(correlation.get(remaining.get(index)));
            }
			clustering.put(remaining.get(index), cluster);
			cluster.add(remaining.get(index));
			cluster.removeAll(clusteringSet);
			remaining.removeAll(cluster);
			clusteringSet.addAll(cluster);
			sum = sum + cluster.size();
		}
		return sumClustering(clustering);
	}

	private HashSet<Integer> realCluster(ArrayList<Integer> remaining) {
		ArrayList<Integer> toRemove = new ArrayList<Integer>();
		HashSet<Integer> cluster = new HashSet<Integer>(remaining);
		for(int movieId_i : remaining){
			int size = remaining.size();
			int counter=0;
			for (int movieId_j : remaining)
			{
				if ((movieId_i != movieId_j) && !toRemove.contains(movieId_i)){
					if (correlation.get(movieId_i).contains(movieId_j)){
						counter++;
					}
				}
			}
			if (counter < size/2){
				toRemove.add(movieId_i);
			}
		}
		cluster.removeAll(toRemove);
		return cluster;

	}

	public double calculateCost(HashSet<Integer> cluster, double[] p_mArray ,  List<Integer> selectedMoviesIds,
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

	public double sumClustering(HashMap<Integer, HashSet<Integer>> clustering)
	{
		double sumClustering = 0;
		int sum = 0;
		for (Integer movieID : clustering.keySet())
		{
			HashSet<Integer> cluster = new HashSet<Integer>(clustering.get(movieID));
			cluster.add(movieID);
			sum = sum + cluster.size();
			for (int movieId : cluster)
			{
				System.out.print(movieId + " " + processing.getMovieIdToMovieProps().get(movieId).getFirst() + ",");
			}
			System.out.println();
			sumClustering= sumClustering + calculateCost(cluster, p_mArray , processing.getSelectedMoviesIds(), RelationFraction);
		}
		return sumClustering;
	}
}