import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Probs2 {

    public static int NUMBER_OF_MOVIES = 3883;
    public static int NUMBER_OF_USERS = 6040;

    public static TreeMap<Integer, HashSet<Integer>> userIdToMoviesIds = new TreeMap<>();
    public static TreeMap<Integer, HashSet<Integer>> movieIdToUsersIds = new TreeMap<>();
    public static TreeMap<Integer, String> movieIdToMovieName = new TreeMap<>();
    public static HashMap<Integer, ArrayList<Integer>> correlation = new HashMap<>();
    public static HashMap<AbstractMap.SimpleEntry<Integer, Integer>, Double> RelationFraction = new HashMap<>();
    public static HashMap<AbstractMap.SimpleEntry<Integer, Integer>, Double> RelationFracDistance = new HashMap<>();
    public static HashMap<Integer, ArrayList<String>> movieIdToMovieGenres = new HashMap<Integer, ArrayList<String>>();
    public static double normalMin = Integer.MAX_VALUE;
    public static double normalMax = Integer.MIN_VALUE;
    
    public static void main(String[] args) {
    	double start = System.currentTimeMillis();
        String ratingsFileName = "C:\\Users\\Admin\\Desktop/mini_project_clustering/ml-1m/ratings.dat";
        String moviesFileName = "C:\\Users\\Admin\\Desktop/mini_project_clustering/ml-1m/movies.dat";
        //int correlationAlgorithm = Integer.valueOf(args[2]);
        String moviesIdsFileName = "C:\\Users\\Admin\\Desktop/mini_project_clustering/selected_movies_ids_100.txt";
    	   	readMoviesNames(moviesFileName);
    	   	try (Stream<String> stream = Files.lines(Paths.get(ratingsFileName))) {
              stream.forEach(Probs2::processStrings);
    		  List<Integer> selectedMovies = readMoviesIdsFile(moviesIdsFileName);
    		  double[] p_mArray = p_m(selectedMovies);
    		  selectedMovies.forEach(x->Probs2.calculateProbs(x, selectedMovies, p_mArray));
    		  normalDist();
    		  movieIdToMovieGenres.keySet().forEach(x->System.out.println(x +" "+movieIdToMovieGenres.get(x)));
//    		  RelationFraction.keySet().forEach(x->System.out.println(x +" "+RelationFraction.get(x)));
//    		  correlation.keySet().forEach(x->System.out.println(x + " " + correlation.get(x)));
    		  correlationAlgorithm(selectedMovies, p_mArray);
//            userIdToMoviesIds.keySet().forEach(x->System.out.println(x + " " + userIdToMoviesIds.get(x)));
//            movieIdToUsersIds.keySet().forEach(x->System.out.println(x + " " + movieIdToUsersIds.get(x) + " "
//            + movieIdToUsersIds.get(x).size()));
    		  System.out.println(System.currentTimeMillis() - start);
        } catch (IOException e) {
           e.printStackTrace();
        }
    }
    
    private static void readMoviesNames(String fileName)
    {
    	try (Stream<String> stream = Files.lines(Paths.get(fileName), Charset.forName("iso-8859-1")))
    	{
    		for (String line : stream.collect(Collectors.toList()))
    		{
    			String[] rowArray = line.split("::");
    			movieIdToMovieName.putIfAbsent(Integer.valueOf(rowArray[0]), rowArray[1]);
    			//System.out.println(rowArray[2]);
    			movieIdToMovieGenres.put(Integer.valueOf(rowArray[0]), new ArrayList<String>(Arrays.asList(rowArray[2].split("\\|"))));
    		}
    	}
    	
    	catch (IOException e)
    	{
    		e.printStackTrace();
    	}
    		
    }
    
    private static ArrayList<Integer> readMoviesIdsFile(String fileName)
    {
    	ArrayList<Integer> selectedMovies = new ArrayList<Integer>();
    	try (Stream<String> stream = Files.lines(Paths.get(fileName)))
    	{
    		for (String line : stream.collect(Collectors.toList()))
    		{
    			if (movieIdToMovieName.containsKey(Integer.valueOf(line)))
    			{
    				//System.out.println(line);
    				if (movieIdToUsersIds.get(Integer.valueOf(line)) != null 
    						&& movieIdToUsersIds.get(Integer.valueOf(line)).size() >= 10)
    				{
    					selectedMovies.add(Integer.valueOf(line));
    				}
    				
    				else if (movieIdToUsersIds.get(Integer.valueOf(line)) == null)
    				{
    					System.err.println("Movie " + line + " ignored because it has only " + 
    									0 + " ratings");
    				}
    				
    				else
    				{
    					System.err.println("Movie " + line + " ignored because it has only " + 
    				movieIdToUsersIds.get(Integer.valueOf(line)).size() + " ratings");
    				}
    			}
    			
    		
    		}
    	}
    	catch (IOException e)
    	{
    		e.printStackTrace();
    	}
		return selectedMovies;
    }

    private static void processStrings(String str) {
        String[] rowArray = str.split("::");
        int userID = Integer.valueOf(rowArray[0]);
        int movieID = Integer.valueOf(rowArray[1]);

        if (movieIdToUsersIds.containsKey(movieID)) {
            movieIdToUsersIds.get(movieID).add(userID);
        } else {
            HashSet<Integer> set = new HashSet<>();
            set.add(userID);
            movieIdToUsersIds.put(movieID, set);
        }

        if (userIdToMoviesIds.containsKey(userID)) {
            userIdToMoviesIds.get(userID).add(movieID);
        } else {
            HashSet<Integer> set = new HashSet<>();
            set.add(movieID);
            userIdToMoviesIds.put(userID, set);
        }
    }

    private static double[] p_m(List<Integer> selectedMoviesIds) {
    	double[] p_mArray = new double[selectedMoviesIds.size()];
        for (int i = 0 ; i < selectedMoviesIds.size() ; i++) {
	    	double sum = 0;
	        for (int userID : movieIdToUsersIds.get(selectedMoviesIds.get(i))) {
	            double n = userIdToMoviesIds.get(userID).size();
	            sum = sum + (2 / n);
	        }
	        p_mArray[i] = (1.0 / (NUMBER_OF_USERS + 1)) * ((2.0 / (NUMBER_OF_MOVIES)) + sum);
        }
        return p_mArray;
    }
    
    private static void calculateProbs(int movieID_i, List<Integer> selectedMoviesIds, double[] p_mArray) {
        for (int movieID_j : selectedMoviesIds) {
        	if (movieID_i < movieID_j) {
        		//System.out.println(movieID_i + " " + movieID_j);
                Set<Integer> temp = new HashSet<>(movieIdToUsersIds.get(movieID_i));
                if (temp.retainAll(movieIdToUsersIds.get(movieID_j))) {
                    double sum = 0;
                    for (int userID : temp) {
                        double n = userIdToMoviesIds.get(userID).size();
                        sum = sum + (2 / (n * (n - 1)));
                    }
                  double sharedP = (1.0 / (NUMBER_OF_USERS + 1)) * ((2.0 / (NUMBER_OF_MOVIES * (NUMBER_OF_MOVIES - 1))) + sum);
                  double firstP = p_mArray[selectedMoviesIds.indexOf(movieID_i)];
                  double secondP = p_mArray[selectedMoviesIds.indexOf(movieID_j)];
                
                    if ((firstP * secondP)<= sharedP)
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
                	RelationFracDistance.put(toInsertPair, sharedP -(firstP * secondP));
                }
            }
        }     
    }
    
    private static void normalDist()
    {
   	 double distanceMin = Integer.MAX_VALUE;
     double distanceMax = Integer.MIN_VALUE;
     
     for (AbstractMap.SimpleEntry<Integer, Integer> fracPair1 : RelationFracDistance.keySet()) {
    	 double frac = RelationFracDistance.get(fracPair1);
    	 if (distanceMin > frac)
    	 {
    		 distanceMin=frac;
    	 }
    	 if (distanceMax < frac)
    	 {
    		 distanceMax=frac;
    	 }
     }
    // System.out.println(distanceMin);
     //System.out.println(distanceMax);
 
     
     for (AbstractMap.SimpleEntry<Integer, Integer> fracPair : RelationFracDistance.keySet()) {
     	double dist = RelationFracDistance.get(fracPair);
     //	System.out.println((dist - distanceMin) + " " + (distanceMax - distanceMin));
     	double normDist = (dist - distanceMin) / (distanceMax - distanceMin);
		   	 if (normalMin > normDist)
			 {
		   		normalMin=normDist;
			 }
			 if (normalMax < normDist)
			 {
				 normalMax=normDist;
			 }
     	
     //	System.out.println(fracPair + " " + normDist);
     	RelationFracDistance.put(fracPair,  normDist);  
    }
   }
    

    private static void correlationAlgorithm(List<Integer> selectedMoviesIds, double[] p_mArray)
    {
    	ArrayList<Integer> remaining = new ArrayList<Integer>(selectedMoviesIds);
    	ArrayList<HashSet<Integer>> clustering = new ArrayList<>();
    	HashSet<Integer> clusteringSet = new HashSet<>();
    	int sum = 0;
    	while (remaining.size() > 0)
    	{
    		HashSet<Integer> cluster = new HashSet<Integer>();
  //          int index = (int) Math.floor(Math.random() * remaining.size());
    		int minUsers = Integer.MAX_VALUE;
    		int indexOfMin = 0;
    		for (int i = 0 ; i < remaining.size() ; i++)
    		{
    			if (minUsers > movieIdToUsersIds.get(remaining.get(i)).size())
				{
    				minUsers = movieIdToUsersIds.get(remaining.get(i)).size();
    				indexOfMin = i;
				}
    		}
            int index = indexOfMin;
 //          System.out.println(remaining.get(index));
            for (Integer movie : correlation.get(remaining.get(index))){
            	//System.out.println(movie + " " + remaining.get(index));
            	ArrayList<String> currentMovieGenres = new ArrayList<String>(movieIdToMovieGenres.get(movie));
            	currentMovieGenres.removeAll(movieIdToMovieGenres.get(remaining.get(index)));
            	if ((RelationFracDistance.get(new AbstractMap.SimpleEntry<Integer, Integer>(Math.min(movie, remaining.get(index)), 
            			Math.max(movie, remaining.get(index)))) > (normalMin + normalMax)/2 && currentMovieGenres.size() > 0))
            	{
            		cluster.add(movie);
            	}
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
    			System.out.print(movieId + " " + movieIdToMovieName.get(movieId) + ",");
    		}
    		System.out.println();
    		sumClustering= sumClustering+ claculateCost(cluster, p_mArray, selectedMoviesIds);
    		

    	}
    	System.out.println(sumClustering);
    }
    
    private static double claculateCost(HashSet<Integer> cluster, double[] p_mArray, List<Integer> selectedMoviesIds)
    {
    	if (cluster.size() == 1)
    	{
  //  		double res = Math.log(1.0 / p_mArray[(int)cluster.toArray()[0]]);
 //   		System.out.println(res);
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
 //   					System.out.println(movieId_i + " " + movieId_j + " " + RelationFraction.get(new AbstractMap.SimpleEntry<Integer, Integer>(movieId_i, movieId_j)));
    					cost = cost + Math.log(1.0 / RelationFraction.get(new AbstractMap.SimpleEntry<Integer, Integer>(movieId_i, movieId_j)));
    				}
    			}
    		}
//    		System.out.println(cost * coeff);
    		return cost * coeff;
    	}
    	
    }
}







