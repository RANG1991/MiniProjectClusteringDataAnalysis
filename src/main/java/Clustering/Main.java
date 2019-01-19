package Clustering;

public class Main {
	public static void main (String[] args) throws Exception
	{
		String datasetFolder = args[1];
        int correlationAlgorithm = Integer.valueOf(args[2]);
        String moviesIdsFileName = args[3];
        String ratingsFileName = datasetFolder + "//ratings.dat";
        String moviesFileName = datasetFolder + "//movies.dat";

        Processing processing = new Processing();
        processing.readMoviesFile(moviesFileName);
        processing.readRatingsFile(ratingsFileName);
        processing.readMoviesIdsFile(moviesIdsFileName);
        processing.readRatingsFileIncludeRating(ratingsFileName);

        if (correlationAlgorithm == 1)
        {
            CorrelationClassicAlgorithm algorithm = new CorrelationClassicAlgorithm(processing);
            System.out.println(algorithm.runAlgorithm());
        }

        else if (correlationAlgorithm == 2)
        {
            CorrelationAlgorithmImprovement algorithm = new CorrelationAlgorithmImprovement(processing);
            System.out.println(algorithm.runAlgorithm());
        }

        else
        {
            throw new Exception("Argument number 2 should be only 1 or 2!");
        }

	}
}