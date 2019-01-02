package Clustering;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Random;

public class CorrelationAlgorithmImprovement {
	
	private Processing processing;
	private  HashMap<Integer, ArrayList<Integer>> correlation = new HashMap<>();
    private HashMap<AbstractMap.SimpleEntry<Integer, Integer>, Double> RelationFraction = new HashMap<>();
    private HashMap<AbstractMap.SimpleEntry<Integer,Integer>, Triplet<Double, Double, Double>> distanceMatrix = 
    		new HashMap<>();
    private double[] p_mArray;
    //private HashMap<Integer, Triplet<Double, Double, Double>>
    private double maxGen = 0;
    private double minGen = Double.MAX_VALUE;
    private double minP = Double.MAX_VALUE;
    private double maxP = Double.MIN_VALUE;
    private double minYears = Double.MAX_VALUE;
    private double maxYears = 0;
	
	public CorrelationAlgorithmImprovement(Processing processing)
	{
		this.processing = processing;
	}
	
	public void runAlgorithm()
	{
		p_mArray = processing.p_m(processing.getSelectedMoviesIds());
		processing.getSelectedMoviesIds().forEach(x->calculateProbs(x, processing.getSelectedMoviesIds(), p_mArray));
		for (AbstractMap.SimpleEntry<Integer, Integer> entry : distanceMatrix.keySet())
		{
			distanceMatrix.get(entry).setFirst(normalizeValues(distanceMatrix.get(entry).getFirst() , minP , maxP)) ;
			distanceMatrix.get(entry).setSecond(normalizeValues(distanceMatrix.get(entry).getSecond() , minGen , maxGen)) ;
			distanceMatrix.get(entry).setThird(normalizeValues(distanceMatrix.get(entry).getThird() , minYears , maxYears)) ;
		}
		/*
		double maxFinalVal=0;
		int first_id=0;
		int second_id=0;
		for (AbstractMap.SimpleEntry<Integer, Integer> entry : distanceMatrix.keySet())
		{
			double finalVal=0.6*distanceMatrix.get(entry).getFirst()+0.3*distanceMatrix.get(entry).getSecond()+0.1*(maxYears-distanceMatrix.get(entry).getThird());
			System.out.println("The finalVal finalVal is: " + finalVal);
			if (finalVal > maxFinalVal) {
				maxFinalVal=finalVal;
				first_id=entry;
				second_identry;
			}

		}*/
		
//		for (AbstractMap.SimpleEntry<Integer, Integer> entry : distanceMatrix.keySet())
//		{
//			System.out.println("The first movie is: " + entry.getKey());
//			System.out.println("The second movie is: " + entry.getValue());
//			System.out.println(distanceMatrix.get(entry));
//		}
//		
//		CorrelationAlgorithmImprovement.correlationAlgorithm(processing.getSelectedMoviesIds(), p_mArray, this.correlation, this.processing,
//				this.RelationFraction);
		KMeans(20);
	}
	
	public ArrayList<Triplet<Double, List<String>, Double>> generateRandomCentroids(int numberOfClusters, int test)
	{
//		if (test==1) {
//		ArrayList<Triplet<Double, Double, Double>> centroids = new ArrayList<Triplet<Double, Double, Double>>();
//		for (int  i = 0; i < numberOfClusters; i++)
//		{
//		    Random rand = new Random();
//		    double randomGenre = minGen + (maxGen - minGen) * rand.nextDouble();
//		    double randomYear = minYears + (maxYears - minYears) * rand.nextDouble();
//		    double randomP = minP + (maxP - minP) * rand.nextDouble();
//		    centroids.add(new Triplet<Double, Double, Double>(randomP, randomGenre, randomYear));
//		}
//		System.out.println(centroids);
//		return centroids;
//		}
		ArrayList<Triplet<Double, List<String>, Double>> centroids = new ArrayList<Triplet<Double, List<String>, Double>>();
		for (int  i = 0; i < numberOfClusters; i++)
		{
			int index = (int) Math.floor(Math.random() * processing.getSelectedMoviesIds().size());
			int movie=processing.getSelectedMoviesIds().get(index);
			double randomP=p_mArray[index];
			List<String>randomGenre=processing.getMovieIdToMovieProps().get(movie).getSecond();
			double randomYear=processing.getMovieIdToMovieProps().get(movie).getThird();
	
		    centroids.add(new Triplet<Double, List<String>, Double>(randomP, randomGenre, randomYear));
		}
		System.out.println(centroids);
		return centroids;
		
		
	}
	
	
	public void calculateProbs(int movieID_i, List<Integer> selectedMoviesIds, double[] p_mArray) {
		for (int movieID_j : selectedMoviesIds) {
			if (movieID_i < movieID_j) {
				//System.out.println(movieID_i + " " + movieID_j);
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
					
					sharedP = Math.log(sharedP);
					
					double firstP = Math.log(p_mArray[selectedMoviesIds.indexOf(movieID_i)]);
					double secondP = Math.log(p_mArray[selectedMoviesIds.indexOf(movieID_j)]);
					
					double yearMovieI = processing.getMovieIdToMovieProps().get(movieID_i).getThird();
					double yearMovieJ = processing.getMovieIdToMovieProps().get(movieID_j).getThird();
					
					List<String> genresI = processing.getMovieIdToMovieProps().get(movieID_i).getSecond();
					List<String> genresJ = new ArrayList<String>(processing.getMovieIdToMovieProps().get(movieID_j).getSecond());
					
					genresJ.retainAll(genresI);
					
					if (genresJ.size() > maxGen) {
						maxGen=genresJ.size();
					}
					if (genresJ.size() < minGen) {
						minGen=genresJ.size();
					} 
					
//					if (sharedP - (firstP * secondP) > maxP) {
//						maxP = sharedP - (firstP * secondP);
//					}
//					if (sharedP - (firstP * secondP) < minP) {
//						minP = sharedP - (firstP * secondP);
//					}
					
					if (firstP > maxP) {
						maxP = firstP;
					}
					if (firstP < minP) {
						minP = firstP;
					} 
					
//					if (Math.abs(yearMovieI - yearMovieJ) > maxYears) {
//						maxYears = Math.abs(yearMovieI - yearMovieJ);
//					}
//					if (Math.abs(yearMovieI - yearMovieJ) < minYears) {
//						minYears = Math.abs(yearMovieI - yearMovieJ);
//					}
					
					if (yearMovieI > maxYears) {
						maxYears = yearMovieI;
					}
					if (yearMovieI < minYears) {
						minYears = yearMovieI;
					}

					distanceMatrix.put(new AbstractMap.SimpleEntry<Integer, Integer>(movieID_i, movieID_j), 
							new Triplet<Double, Double, Double>(sharedP - (firstP * secondP), 
									(double)genresJ.size(), Math.abs(yearMovieI-yearMovieJ)));
					
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

	public static void correlationAlgorithm(List<Integer> selectedMoviesIds, double[] p_mArray,
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
			int index = (int) Math.floor(Math.random() * remaining.size());
			//System.out.println(remaining.get(index));
			cluster.addAll(correlation.get(remaining.get(index)));
			//System.out.println(correlation.get(remaining.get(index)));
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
				System.out.print(movieId + " " + processing.getMovieIdToMovieProps().get(movieId).getFirst() + ",");
			}
			System.out.println();
			sumClustering= sumClustering + claculateCost(cluster, p_mArray , selectedMoviesIds, RelationFraction);
		}
		System.out.println(sumClustering);
	}
	
	private double normalizeValues(double value, double min, double max)
	{
		return ((value - min) / (max - min));
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
						if (!RelationFraction.containsKey(new AbstractMap.SimpleEntry<Integer, Integer>(movieId_i, movieId_j)))
						{
							System.out.println(movieId_i + " " + movieId_j);
						}
						cost = cost + Math.log(1.0 / 
								Math.exp(RelationFraction.get(new AbstractMap.SimpleEntry<Integer, Integer>(movieId_i, movieId_j))));
					}
				}
			}
			return cost * coeff;
		}
	}
	
	public double calculateDistance(Triplet<Double, Double, Double> trip1, Triplet<Double, Double, Double> trip2)
	{
		double dist1 = Math.pow(trip1.getFirst() - trip2.getFirst(), 2);
		double dist2 = Math.pow(trip1.getSecond() - trip2.getSecond(), 2);
		double dist3 = Math.pow(trip1.getThird() - trip2.getThird(), 2);
		return Math.sqrt(dist1 + dist2 + dist3);
	}
	
	public Triplet<Double, Double, Double> calculateNewCentroid(Triplet<Double, Double, Double> centroid, HashSet<Integer> movies)
	{
		if (movies.size() > 0)
		{
		double sumP = 0.0;
		double sumG = 0.0;
		double sumY = 0.0;
		double sizeAll = movies.size();
		for (Integer movie : movies)
		{
			//System.out.println(normalizeValues(processing.getMovieIdToMovieProps().get(movie).getSecond().size(), minGen, maxGen));
			sumG = sumG + normalizeValues(processing.getMovieIdToMovieProps().get(movie).getSecond().size(), minGen, maxGen);
			//System.out.println("sumG in loop: "  + sumG);
			sumY = sumY + normalizeValues(processing.getMovieIdToMovieProps().get(movie).getThird(), minYears, maxYears);
			sumP = sumP + Math.log(normalizeValues(p_mArray[processing.getSelectedMoviesIds().indexOf(movie)], minP, maxP));
		}
		//System.out.println("sumG out loop: " + sumG);
		return new Triplet<Double, Double, Double>(sumP / sizeAll, sumG / sizeAll, sumY / sizeAll);
		}
		else
		{
			return centroid;
		}
	}
	
	public void KMeans(int K)
	{
		//ArrayList<Triplet<Double, Double, Double>> centroids = generateRandomCentroids(K);
		ArrayList<Triplet<Double, List<String>, Double>> centroids = generateRandomCentroids(K,2);
		for (int j = 0 ; j < 100 ; j++)
		{
			HashMap<Triplet<Double, Double, Double>, HashSet<Integer>> cetroidsGroups = new HashMap<>();
			for (int i = 0; i < K; i++)
			{
		//		cetroidsGroups.put(centroids.get(i), new HashSet<Integer>());
			}
			for (Integer movie : processing.getSelectedMoviesIds())
			{
				double sizeOfGenres = normalizeValues(processing.getMovieIdToMovieProps().get(movie).getSecond().size(), minGen, maxGen);
				double year = normalizeValues(processing.getMovieIdToMovieProps().get(movie).getThird(), minYears, maxYears);
				double  p_m = Math.log(normalizeValues(p_mArray[processing.getSelectedMoviesIds().indexOf(movie)], minP, maxP));
				Triplet<Double, Double, Double> movieTriplet = new Triplet<>(p_m, sizeOfGenres, year);
				double minDist = Double.MAX_VALUE;
				int minIndexOfCentroids = -1;
				for (int i = 0; i < K; i++)
				{
					//System.out.println(centroids.get(i) + " " + minIndexOfCentroids + " " + i);
			//		double dist = calculateDistance(movieTriplet, centroids.get(i));
					//System.out.println(dist);
			//		if (dist < minDist)
					{
						minIndexOfCentroids = i;
			//			minDist = dist;
					}
				}
				cetroidsGroups.get(centroids.get(minIndexOfCentroids)).add(movie);
			}
			ArrayList<Triplet<Double, Double, Double>> newCentroids = new ArrayList<Triplet<Double, Double, Double>>();
			for (int i = 0; i < K; i++)
			{
		//		newCentroids.add(calculateNewCentroid(centroids.get(i), cetroidsGroups.get(centroids.get(i))));
			}
			double sumClustering = 0;
			for (Triplet<Double, Double, Double> centroid : cetroidsGroups.keySet())
			{
				HashSet<Integer> cluster = cetroidsGroups.get(centroid);
				for (int movieId : cluster)
				{
					//System.out.print(movieId + " " + processing.getMovieIdToMovieProps().get(movieId).getFirst() + ",");
				}
				//System.out.println();
				sumClustering= sumClustering + claculateCost(cluster, p_mArray , processing.getSelectedMoviesIds(), RelationFraction);
			}
			System.out.println(sumClustering);
		//	centroids = newCentroids;
			System.out.println(centroids);
		}
	}
}
