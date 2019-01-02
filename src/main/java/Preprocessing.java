import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.sql.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

public class Preprocessing {

    public static int NUMBER_OF_MOVIES = 3883;
    public static int NUMBER_OF_USERS = 6040;

    public static TreeMap<Integer, HashSet<Integer>> userIdToMoviesIds = new TreeMap<>();
    public static TreeMap<Integer, HashSet<Integer>> movieIdToUsersIds = new TreeMap<>();
    public static String[][] probs = new String[3952][3952];

    public static void main(String[] args) {
//        String fileName = "/Users/ran/Desktop/ml-1m/ratings.dat";
//        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
//        	createNewDatabase("Clustering.db");
//            createNewTable();
//            stream.forEach(Preprocessing::processStrings);
//            movieIdToUsersIds.keySet().forEach(Preprocessing::calculateProbs);
//            writeToCSVFile("/Users/ran/Desktop/probs.csv");
//            userIdToMoviesIds.keySet().forEach(x->System.out.println(x + " " + userIdToMoviesIds.get(x)));
//            movieIdToUsersIds.keySet().forEach(x->System.out.println(x + " " + movieIdToUsersIds.get(x) + " "
//            + movieIdToUsersIds.get(x).size()));
//            Files.write(Paths.get("prob.txt"), Arrays.deepToString(probs).getBytes());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
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

    private static void calculateProbs(int movieID_i) {
        for (int movieID_j : movieIdToUsersIds.keySet()) {
            if (movieID_i == movieID_j) {
                if (movieIdToUsersIds.get(movieID_i).size() < 10) {
                    continue; //Pay Attention to this!!!!
                }
                double sum = 0;
                for (int userID : movieIdToUsersIds.get(movieID_i)) {
                    double n = userIdToMoviesIds.get(userID).size();
                    sum = sum + (2 / n);
                }

                probs[movieID_i - 1][movieID_j - 1] = String.valueOf((1.0 / (NUMBER_OF_USERS + 1)) * ((2.0 / (NUMBER_OF_MOVIES)) + sum));

            } else if (movieID_i < movieID_j) {
                Set<Integer> temp = new HashSet<>(movieIdToUsersIds.get(movieID_i));
                if (temp.retainAll(movieIdToUsersIds.get(movieID_j))) {
                    double sum = 0;
                    for (int userID : temp) {
                        double n = userIdToMoviesIds.get(userID).size();
                        sum = sum + (2 / (n * (n - 1)));
                    }
                    probs[movieID_i - 1][movieID_j - 1] = String.valueOf((1.0 / (NUMBER_OF_USERS + 1)) * ((2.0 / (NUMBER_OF_MOVIES * (NUMBER_OF_MOVIES - 1))) + sum));
                }
            }
        }
    }

    private static void createNewDatabase(String fileName) {

        String url = "jdbc:sqlite:/users/studs/bsc/2016/ranga/Desktop/" + fileName;

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void createNewTable() {
        // SQLite connection string
        String url = "jdbc:sqlite:/users/studs/bsc/2016/ranga/Desktop/Clustering.db";

        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS moviesProbs (id text primary key not null);";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}







