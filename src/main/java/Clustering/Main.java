package Clustering;

public class Main {
	public static void main (String[] args)
	{
		double start = System.currentTimeMillis();
        String ratingsFileName = "/users/studs/bsc/2016/ranga/Desktop/mini_project_clustering/ml-1m/ratings.dat";
        String moviesFileName = "/users/studs/bsc/2016/ranga/Desktop/mini_project_clustering/ml-1m/movies.dat";
        int correlationAlgorithm = 1;
        String moviesIdsFileName = "/users/studs/bsc/2016/ranga/Desktop/mini_project_clustering/selected_movies_ids_100.txt";
        
        Processing processing = new Processing();
        processing.readMoviesFile(moviesFileName);
        processing.readRatingsFile(ratingsFileName);
        processing.readMoviesIdsFile(moviesIdsFileName);
        processing.readRatingsFileIncludeRating(ratingsFileName);
//        for (Integer entry : processing.getMovieIdToUsersIdsIncludingRatings().keySet())
//        {
//            System.out.println(processing.getMovieIdToUsersIdsIncludingRatings().get(entry));
//        }
        
//
//
//        	CorrelationClassicAlgorithm classicAlgo = new CorrelationClassicAlgorithm(processing);
//        	classicAlgo.runAlgorithm();


//
//
//        if (correlationAlgorithm == 2)
//        {
//        	CorrelationAlgorithmImprovement improAlgo = new CorrelationAlgorithmImprovement(processing);
//        	improAlgo.runAlgorithm();
//        }

        ShakedImprovement s = new ShakedImprovement(processing);
        s.runAlgorithm();
        
        System.out.println(System.currentTimeMillis() - start);
	}

}
