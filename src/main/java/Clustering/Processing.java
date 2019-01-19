package Clustering;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Processing {
	
	private int numberOfMovies;
	private int numberOfSelctedMovies;
	private int numberOfUsers;

	private TreeMap<Integer, HashSet<Integer>> userIdToMoviesIds = new TreeMap<>();
	private TreeMap<Integer, HashSet<Integer>> movieIdToUsersIds = new TreeMap<>();
	private HashMap<Integer, HashMap<Integer, Integer>> movieIdToUsersIdsIncludingRatings
	= new HashMap<>();
	private ArrayList<String> allGenres = new ArrayList<>();
	//TreeMap that its keys are the movie id and its values are the movie name, the movie genres and the movie year.
	private TreeMap<Integer, Triplet<String, List<String>, Integer>> movieIdToMovieProps = new TreeMap<>();
	private ArrayList<Integer> selectedMoviesIds = new ArrayList<Integer>();
	

	public Processing()
	{
		this.numberOfMovies = 3883;
		this.numberOfSelctedMovies = 100;
		this.numberOfUsers = 6040;
	}

	public void readMoviesFile(String fileName)
	{
		try (Stream<String> stream = Files.lines(Paths.get(fileName), Charset.forName("iso-8859-1")))
		{
			for (String line : stream.collect(Collectors.toList()))
			{
				String[] rowArray = line.split("::");
				Pattern p = Pattern.compile("\\((\\d+)\\)");
				Matcher m = p.matcher(rowArray[1]);
				m.find();
				int year = Integer.valueOf(m.group(1));
				movieIdToMovieProps.putIfAbsent(Integer.valueOf(rowArray[0]), 
						new Triplet<String, List<String>, Integer>(
								rowArray[1],
								Arrays.asList(rowArray[2].split("\\|")),
								year));
				for (String genre : Arrays.asList(rowArray[2].split("\\|")))
				{
					if (!this.allGenres.contains(genre))
					{
						this.allGenres.add(genre);
					}
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	public void readMoviesIdsFile(String fileName)
	{
		try (Stream<String> stream = Files.lines(Paths.get(fileName)))
		{
			List<String> listOfAllLines = stream.collect(Collectors.toList());
			for (String line : listOfAllLines)
			{
				if (movieIdToMovieProps.containsKey(Integer.valueOf(line)))
				{
					if (movieIdToUsersIds.get(Integer.valueOf(line)) != null 
							&& movieIdToUsersIds.get(Integer.valueOf(line)).size() >= 10)
					{
						getSelectedMoviesIds().add(Integer.valueOf(line));
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
			this.numberOfSelctedMovies = getSelectedMoviesIds().size();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void readRatingsFile(String fileName) {

		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
			for (String line : stream.collect(Collectors.toList()))
			{
				String[] rowArray = line.split("::");
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
			this.numberOfMovies = movieIdToUsersIds.keySet().size();
			this.numberOfUsers = userIdToMoviesIds.keySet().size();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void readRatingsFileIncludeRating(String fileName) {

		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
			for (String line : stream.collect(Collectors.toList()))
			{
				String[] rowArray = line.split("::");
				int userID = Integer.valueOf(rowArray[0]);
				int movieID = Integer.valueOf(rowArray[1]);
				int rating = Integer.valueOf(rowArray[2]);

				if (movieIdToUsersIdsIncludingRatings.containsKey(movieID))
				{
					movieIdToUsersIdsIncludingRatings.get(movieID).put(userID, rating);
				}
				else
				{
					HashMap<Integer, Integer> map = new HashMap<>();
					map.put(userID, rating);
					movieIdToUsersIdsIncludingRatings.put(movieID, map);
				}


				if (userIdToMoviesIds.containsKey(userID)) {
					userIdToMoviesIds.get(userID).add(movieID);
				} else {
					HashSet<Integer> set = new HashSet<>();
					set.add(movieID);
					userIdToMoviesIds.put(userID, set);
				}
			}
			this.numberOfMovies = movieIdToUsersIds.keySet().size();
			this.numberOfUsers = userIdToMoviesIds.keySet().size();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public double[] p_m(List<Integer> selectedMoviesIds) {
		double[] p_mArray = new double[selectedMoviesIds.size()];
		for (int i = 0 ; i < selectedMoviesIds.size() ; i++) {
			double sum = 0;
			for (int userID :  this.movieIdToUsersIds.get(selectedMoviesIds.get(i))) {
				double n = this.userIdToMoviesIds.get(userID).size();
				sum = sum + (2 / n);
			}
			p_mArray[i] = (1.0 / (this.numberOfUsers + 1)) * ((2.0 / (this.numberOfMovies)) + sum);
		}
		return p_mArray;
	}

	public TreeMap<Integer, HashSet<Integer>> getUserIdToMoviesIds() {
		return userIdToMoviesIds;
	}
	
	public TreeMap<Integer, HashSet<Integer>> getMovieIdToUsersIds() {
		return movieIdToUsersIds;
	}
	
	public TreeMap<Integer, Triplet<String, List<String>,Integer>> getMovieIdToMovieProps() {
		return movieIdToMovieProps;
	}

	public int getNumberOfMovies() {
		return numberOfMovies;
	}

	public int getNumberOfSelctedMovies() {
		return numberOfSelctedMovies;
	}

	public int getNumberOfUsers() {
		return numberOfUsers;
	}

	public void setSelectedMoviesIds(ArrayList<Integer> newSelectedMoviesIds) {
		selectedMoviesIds = newSelectedMoviesIds;
	}

	public ArrayList<Integer> getSelectedMoviesIds() {
		return selectedMoviesIds;
	}

	public void setSelectedMoviesIds(ArrayList<Integer> moviesList)
	{
		selectedMoviesIds = moviesList;
	}

	public HashMap<Integer, HashMap<Integer, Integer>> getMovieIdToUsersIdsIncludingRatings()
	{
		return this.movieIdToUsersIdsIncludingRatings;
	}

	public ArrayList<String> getAllGenres()
	{
		return this.allGenres;
	}
}

