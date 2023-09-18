package edu.cmu.cs.cs214.dataPlugins;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class fetch lyrics of given track from Musixmatch API.
 */
public class LyricsFetcher {
    private String musixmatchApiKey;
    private static String CREDENTIALS_PATH = "src/main/java/edu/cmu/cs/cs214/credentials.json";

    /**
     * Constructor of LyricsFetcher.
     */
    public LyricsFetcher() {
        readMusixmatchApiKey();
    }

    /**
     * Read Musixmatch API key from credentials.json file.
     */
    public void readMusixmatchApiKey() {
        try {
            String credentials = new String(Files.readAllBytes(Paths.get(CREDENTIALS_PATH)));
            JSONObject credentialsJson = new JSONObject(credentials);
            musixmatchApiKey = credentialsJson.getString("musixmatchApiKey");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println("Error reading Musixmatch API key.");
        }
    }

    /**
     * Fetch lyrics of given track from Musixmatch API.
     *
     * @param title  title of the track
     * @param artist artist of the track
     * @return lyrics of the track
     */
    public String fetchLyrics(String title, String artist) {
        try {
            String baseUrl = "https://api.musixmatch.com/ws/1.1";
            String trackSearchUrl = baseUrl + "/matcher.track.get?q_track=%s&q_artist=%s&apikey=%s";
            String encodedTitle = java.net.URLEncoder.encode(title, "UTF-8");
            String encodedArtist = java.net.URLEncoder.encode(artist, "UTF-8");
            String requestUrl = String.format(trackSearchUrl, encodedTitle, encodedArtist, musixmatchApiKey);

            HttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(requestUrl);

            HttpResponse response = httpClient.execute(httpGet);
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            JSONObject responseObject = new JSONObject(responseBody);
            JSONObject trackObject = responseObject.getJSONObject("message").getJSONObject("body")
                    .getJSONObject("track");
            int hasLyrics = trackObject.getInt("has_lyrics");

            if (hasLyrics == 1) {
                int trackId = trackObject.getInt("track_id");
                String lyricsSearchUrl = baseUrl + "/track.lyrics.get?track_id=%d&apikey=%s";
                requestUrl = String.format(lyricsSearchUrl, trackId, musixmatchApiKey);

                httpGet = new HttpGet(requestUrl);
                response = httpClient.execute(httpGet);
                responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                responseObject = new JSONObject(responseBody);
                String lyrics = responseObject.getJSONObject("message").getJSONObject("body").getJSONObject("lyrics")
                        .getString("lyrics_body");
                return lyrics;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Error fetching lyrics.");
        }

        return "";
    }

    /**
     * This main method shows what the fetched lyrics from Musixmatch API looks like.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        LyricsFetcher lyricsFetcher = new LyricsFetcher();

        String title = "Bohemian Rhapsody";
        String artist = "Queen";

        String lyrics = lyricsFetcher.fetchLyrics(title, artist);

        System.out.println("Lyrics for " + title + " by " + artist + ":");
        System.out.println(lyrics);
    }

}
