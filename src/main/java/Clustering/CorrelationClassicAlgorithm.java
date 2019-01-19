package Clustering;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CorrelationClassicAlgorithm {


	private Processing processing;
	private  HashMap<Integer, ArrayList<Integer>> correlation = new HashMap<>();
    private HashMap<AbstractMap.SimpleEntry<Integer, Integer>, Double> RelationFraction = new HashMap<>();

	public CorrelationClassicAlgorithm(Processing processing)
	{
		this.processing = processing;
	}

	public double runAlgorithm()
	{
		double[] p_mArray = processing.p_m(processing.getSelectedMoviesIds());
		processing.getSelectedMoviesIds().forEach(x->calculateProbs(x, processing.getSelectedMoviesIds(), p_mArray));
		double sumClustering = correlationAlgorithm(processing.getSelectedMoviesIds(), p_mArray, this.correlation, this.processing,
				this.RelationFraction);
		return sumClustering;
	}
	

	public void calculateProbs(int movieID_i, List<Integer> selectedMoviesIds, double[] p_mArray) {
		for (int movieID_j : selectedMoviesIds) {
			if (movieID_i < movieID_j) {
				Set<Integer> temp = new HashSet<>(processing.getMovieIdToUsersIds().get(movieID_i));
				temp.retainAll(processing.getMovieIdToUsersIds().get(movieID_j));
				//if (temp.size() > 0) {
					double sum = 0;
					for (int userID : temp) {
						double n = processing.getUserIdToMoviesIds().get(userID).size();
						sum = sum + (2 / (n * (n - 1)));
					}
					double sharedP = (1.0 / (processing.getNumberOfUsers() + 1)) *
							((2.0 / (processing.getNumberOfMovies() * (processing.getNumberOfMovies() - 1))) + sum);
					double firstP = p_mArray[selectedMoviesIds.indexOf(movieID_i)];
					double secondP = p_mArray[selectedMoviesIds.indexOf(movieID_j)];

					if ((firstP * secondP) <= sharedP) {
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
				AbstractMap.SimpleEntry<Integer, Integer> toInsertPair = new AbstractMap.SimpleEntry<Integer, Integer>(movieID_i, movieID_j);
				RelationFraction.put(toInsertPair, sharedP);
			}
		}
	}

	public static double correlationAlgorithm(List<Integer> selectedMoviesIds, double[] p_mArray,
			HashMap<Integer, ArrayList<Integer>> correlation, Processing processing,
			HashMap<AbstractMap.SimpleEntry<Integer, Integer>, Double> RelationFraction)
	{
		ArrayList<Integer> remaining = new ArrayList<Integer>(selectedMoviesIds);
		ArrayList<HashSet<Integer>> clustering = new ArrayList<>();
		HashSet<Integer> clusteringSet = new HashSet<>();
		int sum = 0;
		while (remaining.size() > 0)
		{
			HashSet<Integer> cluster = new HashSet<Integer>();
			int index = (int) Math.floor(Math.random() * (remaining.size()));
			if (correlation.containsKey(remaining.get(index)))
			{
				cluster.addAll(correlation.get(remaining.get(index)));
			}
			cluster.add(remaining.get(index));
			cluster.removeAll(clusteringSet);
			remaining.removeAll(cluster);
			clusteringSet.addAll(cluster);
			clustering.add(cluster);
			sum = sum + cluster.size();
		}
		double sumClustering = 0;
		for (HashSet<Integer> cluster : clustering)
		{
			for (int movieId : cluster)
			{
			//	System.out.print(movieId + " " + processing.getMovieIdToMovieProps().get(movieId).getFirst() + ",");
			}
			//System.out.println();
			sumClustering= sumClustering+ calculateCost(cluster, p_mArray , selectedMoviesIds, RelationFraction);

		}
		return sumClustering;
	}

	public static double calculateCost(HashSet<Integer> cluster, double[] p_mArray , List<Integer> selectedMoviesIds,
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
								RelationFraction.get(new AbstractMap.SimpleEntry<Integer, Integer>(movieId_i, movieId_j)));
					}
				}
			}
			return cost * coeff;
		}
	}
}
