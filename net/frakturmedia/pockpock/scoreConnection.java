// scoreConnection.java
// Handles the internet connection to get the high scores
// and upload a higher score

package net.frakturmedia.pockpock;

import java.io.*;
import java.net.*;
import java.util.*;

public class scoreConnection {

    final String HIGH_SCORE_URL = "http://frakturmedia.net/pockpock/pock_pock_highscores.txt";
    final String UPDATE_SCORE_URL = "http://frakturmedia.net/pockpock/pp_new_highscore.php";

    public static void main (String args[]) {
        //new scoreConnection();
    }

    scoreConnection() {
        //System.out.println(getSetHighScores(""));

        //System.out.println(Arrays.deepToString(getSetHighScores("")));

        //System.out.println(getSetHighScores("Briac-69.2"));
    }

    public String[][] getSetHighScores(String payload) {
        String line, path, response = "Failed to load scores.";

        //create path with appended payload
        if ( payload != "" ) {
            path = UPDATE_SCORE_URL + "?data=" + payload;
        } else {
            path = HIGH_SCORE_URL;
        }

        try {
            var url = URI.create(path).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            if( conn.getResponseCode() == HttpURLConnection.HTTP_OK ) {
                    InputStream is = conn.getInputStream();
                    response = convertStreamToString(is);
            } else {
                    InputStream err = conn.getErrorStream();
                    // err may have useful information.. but could be null see javadocs for more information
                    System.out.println("Response code not OK.");
                    // System.out.println(convertStreamToString(err));
            }
        } catch (IOException e) { 
            // Something horrible happened, as in a network error, or you
            // foolishly called getResponseCode() before HUC was ready.
            // Essentially no methods of on "conn" now work, so don't go
            // looking for help there.
            System.out.println("Establishing connection failed.");
            //System.out.println(e);
        }

        String[] net_row_data = response.split("\r\n");
        String[][] net_hs = new String[net_row_data.length][2];

        int i = 0;
        for( i = 0; i < net_row_data.length; i += 1 ) {
            if (net_row_data[i].contains("&")) {
                net_hs[i] = net_row_data[i].split("&");
            }
        }

        return net_hs;
    }

    public static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
