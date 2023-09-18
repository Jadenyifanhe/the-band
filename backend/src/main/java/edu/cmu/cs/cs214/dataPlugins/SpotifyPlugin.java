package edu.cmu.cs.cs214.dataPlugins;

import edu.cmu.cs.cs214.core.MelodyFramework;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.requests.data.artists.GetArtistRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * This class is a data plugin that fetches data from Spotify API.
 * Please set the path to your credentials.json file in CREDENTIALS_PATH.
 */
public class SpotifyPlugin implements DataPlugin {
    private static final String NAME = "Spotify";
    // set the path to your credentials.json file
    // private static final String CREDENTIALS_PATH = "/path/to/credentials.json";
    private static String CREDENTIALS_PATH = "src/main/java/edu/cmu/cs/cs214/credentials.json";
    private String playlistUrl = "https://open.spotify.com/playlist/37i9dQZF1DXcF6B6QPhFDv";
    private SpotifyApi spotifyApi;
    private String accessToken;
    private JSONObject spotifyCredentials;
    private MelodyFramework framework;
    private LyricsFetcher lyricsFetcher;

    /**
     * Constructor for SpotifyPlugin.
     */
    public SpotifyPlugin() {
        readSpotifyCredentials();
        setupSpotifyApi();
        lyricsFetcher = new LyricsFetcher();
    }

    /**
     * This method get the name of this data plugin.
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * This method is called when the plugin is registered to the framework.
     */
    @Override
    public void onRegister(MelodyFramework framework) {
        this.framework = framework;
    }

    /**
     * This method returns the  access token.
     *
     * @param isDefault whether the default access token should be returned
     * @return the access token
     */
    @Override
    public String getAccessToken(boolean isDefault) {
        return accessToken;
    }

    /**
     * This method fetch data from Spotify.
     *
     * @param accessToken the accessToken to use Spotify API
     * @return a list of tracks objects
     */
    @Override
    public List<Track> getData(String accessToken) {
        System.out.println("Fetching data from Spotify API. Please wait...");

        String playlistId = extractPlaylistIdFromUrl(playlistUrl);
        List<Track> trackList = new ArrayList<>();

        try {
            GetPlaylistsItemsRequest getPlaylistsItemsRequest = spotifyApi.getPlaylistsItems(playlistId).build();
            Paging<PlaylistTrack> playlistTrackPaging = getPlaylistsItemsRequest.execute();

            for (PlaylistTrack playlistTrack : playlistTrackPaging.getItems()) {
                se.michaelthelin.spotify.model_objects.specification.Track track = (se.michaelthelin.spotify.model_objects.specification.Track) playlistTrack
                        .getTrack();
                String title = track.getName();
                String artist = track.getArtists()[0].getName();
                String artistId = track.getArtists()[0].getId();
                List<String> genre = getArtistGenres(artistId);

                Date addedDate = playlistTrack.getAddedAt();
                Instant timeAdded = addedDate.toInstant();

                String lyrics = lyricsFetcher.fetchLyrics(title, artist);
                double[] sentimentScore = framework.analyzeSentimentText(lyrics);

                Track customTrack = new Track(title, artist, genre, timeAdded.toString(), sentimentScore);
                trackList.add(customTrack);
            }
            return trackList;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error fetching data from Spotify API.");
        }
        return trackList;
    }

    /**
     * This method sets the playlist url.
     *
     * @param playlistUrl the playlist url
     */
    public void setPlaylistUrl(String playlistUrl) {
        this.playlistUrl = playlistUrl;
    }

    /**
     * This method extracts the playlist id from the playlist url.
     *
     * @param url the playlist url
     * @return the playlist id
     */
    private String extractPlaylistIdFromUrl(String url) {
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }

    /**
     * This method gets the genres of the artist.
     *
     * @param artistId the artist id
     * @return a list of genres
     */
    private List<String> getArtistGenres(String artistId) {
        GetArtistRequest getArtistRequest = spotifyApi.getArtist(artistId).build();
        try {
            Artist artist = getArtistRequest.execute();
            return Arrays.asList(artist.getGenres());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * This method set up access to Spotify API.
     */
    private void setupSpotifyApi() {
        try {
            String clientId = spotifyCredentials.getString("spotifyClientId");
            String clientSecret = spotifyCredentials.getString("spotifyClientSecret");

            SpotifyApi.Builder builder = new SpotifyApi.Builder()
                    .setClientId(clientId)
                    .setClientSecret(clientSecret);

            spotifyApi = builder.build();
            ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
            ClientCredentials clientCredentials = clientCredentialsRequest.execute();

            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
            this.accessToken = clientCredentials.getAccessToken();
            System.out.println("Successfully set up Spotify API with client credentials.");
        } catch (IOException | SpotifyWebApiException | org.apache.hc.core5.http.ParseException e) {
            System.out.println("Error setting up Spotify API.");
            e.printStackTrace();
        }
    }

    /**
     * This method reads the Spotify credentials from the credentials.json file.
     */
    private void readSpotifyCredentials() {
        try {
            String credentials = new String(Files.readAllBytes(Paths.get(CREDENTIALS_PATH)));
            spotifyCredentials = new JSONObject(credentials);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error reading Spotify API credentials.");
        }
    }

    /**
     * This main method shows what the fetched data from Spotify API looks like.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Replace with a valid playlist URL
        String playlistUrl = "https://open.spotify.com/playlist/37i9dQZF1EQncLwOalG3K7";
        SpotifyPlugin spotifyPlugin = new SpotifyPlugin();
        spotifyPlugin.setPlaylistUrl(playlistUrl);
        MelodyFramework melodyFramework = new MelodyFramework();
        spotifyPlugin.onRegister(melodyFramework);
        boolean isDefault = true;
        String testAccessToken = spotifyPlugin.getAccessToken(isDefault);
        List<Track> tracks = spotifyPlugin.getData(testAccessToken);

        for (Track track : tracks) {
            System.out.println(track);
        }
    }

}
