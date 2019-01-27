package Clustering;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;


public class Main {
	public static void main (String[] args) throws IOException
	{
		double start = System.currentTimeMillis();
        String ratingsFileName = "C:\\Users\\Admin\\Desktop/mini_project_clustering/ml-1m/ratings.dat";
        String moviesFileName = "C:\\Users\\Admin\\Desktop/mini_project_clustering/ml-1m/movies.dat";
        int correlationAlgorithm = 1;
        String moviesIdsFileName = "C:\\Users\\Admin\\Desktop/mini_project_clustering/selected_movies_ids_100.txt";

        HashSet<Integer> allMovies = new HashSet<>();

        for (int i = 0 ; i < 20 ; i++) {

            Processing processing = new Processing();
            processing.readMoviesFile(moviesFileName);
            processing.readRatingsFile(ratingsFileName);
            //processing.setSelectedMoviesIds(generateMoviesArray(processing, 100, allMovies, i));
            processing.readMoviesIdsFile(moviesIdsFileName);
            processing.readRatingsFileIncludeRating(ratingsFileName);

            CorrelationAlgorithmImprovement s = new CorrelationAlgorithmImprovement(processing);

            double sumImp = s.runAlgorithm();

            CorrelationClassicAlgorithm r = new CorrelationClassicAlgorithm(processing);

            double sumReg = r.runAlgorithm();

            System.out.println(sumImp + "\t" + sumReg);
        }
        
        System.out.println(System.currentTimeMillis() - start);
	}

	public static ArrayList<Integer> generateMoviesArray(Processing processing, int numberOfMovies,
                                                         HashSet<Integer> allMovies, int j) throws IOException
    {
        int i = 0;
        ArrayList<Integer> moviesArray = new ArrayList<>();
        PrintWriter writer = new PrintWriter("C:\\Users\\Admin\\Desktop/mini_project_clustering/randomsubset"
                + (j + 1) + ".txt", "UTF-8");
        while (i < numberOfMovies)
        {
            int randomNum = ThreadLocalRandom.current().nextInt(1, processing.getNumberOfMovies());
            if (!allMovies.contains(randomNum) && processing.getMovieIdToUsersIds().keySet().contains(randomNum)
             && processing.getMovieIdToUsersIds().get(randomNum).size() >= 10)
            {
                writer.println(randomNum);
                allMovies.add(randomNum);
                moviesArray.add(randomNum);
                i++;
            }
        }
        writer.close();
        return moviesArray;
    }

}
