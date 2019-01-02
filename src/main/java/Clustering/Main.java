package Clustering;

public class Main {
	public static void main (String[] args)
	{
		double start = System.currentTimeMillis();
        String ratingsFileName = "C:\\Users\\Admin\\Desktop\\mini_project_clustering\\ml-1m\\ratings.dat";
        String moviesFileName = "C:\\Users\\Admin\\Desktop\\mini_project_clustering\\ml-1m\\movies.dat";
        int correlationAlgorithm = 1;
        String moviesIdsFileName = "C:\\Users\\Admin\\Desktop\\mini_project_clustering\\selected_movies_ids_500.txt";
        
        Processing processing = new Processing();
        processing.readMoviesFile(moviesFileName);
        processing.readRatingsFile(ratingsFileName);
        processing.readMoviesIdsFile(moviesIdsFileName);
        processing.readRatingsFileIncludeRating(ratingsFileName);
//
//        for (Integer entry : processing.getMovieIdToUsersIdsIncludingRatings().keySet())
//        {
//            System.out.println(processing.getMovieIdToUsersIdsIncludingRatings().get(entry));
//        }
        


        	CorrelationClassicAlgorithm classicAlgo = new CorrelationClassicAlgorithm(processing);
        	classicAlgo.runAlgorithm();


//
//
//        if (correlationAlgorithm == 2)
//        {
//        	CorrelationAlgorithmImprovement improAlgo = new CorrelationAlgorithmImprovement(processing);
//        	improAlgo.runAlgorithm();
//        }

        RanImprovement r = new RanImprovement(processing);
        r.runAlgorithm();
        
        System.out.println(System.currentTimeMillis() - start);
	}

}
