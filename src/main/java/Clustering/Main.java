package Clustering;

public class Main {
	public static void main (String[] args)
	{
		double start = System.currentTimeMillis();
        String ratingsFileName = "C:\\Users\\Admin\\Desktop/mini_project_clustering/ml-1m/ratings.dat";
        String moviesFileName = "C:\\Users\\Admin\\Desktop/mini_project_clustering/ml-1m/movies.dat";
        int correlationAlgorithm = 1;
        String moviesIdsFileName = "C:\\Users\\Admin\\Desktop/mini_project_clustering/selected_movies_ids_100.txt";
        
        Processing processing = new Processing();
        processing.readMoviesFile(moviesFileName);
        processing.readRatingsFile(ratingsFileName);
        processing.readMoviesIdsFile(moviesIdsFileName);
        processing.readRatingsFileIncludeRating(ratingsFileName);

        CorrelationAlgorithmImprovement s = new CorrelationAlgorithmImprovement(processing);

        s.runAlgorithm();
        
        System.out.println(System.currentTimeMillis() - start);
	}
}
