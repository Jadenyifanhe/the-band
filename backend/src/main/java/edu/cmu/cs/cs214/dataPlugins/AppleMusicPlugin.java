package edu.cmu.cs.cs214.dataPlugins;

import edu.cmu.cs.cs214.core.MelodyFramework;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.interfaces.ECPrivateKey;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * This class is a data plugin that fetches data from Apple Music API.
 * Please set the path to your credentials.json file in CREDENTIALS_PATH.
 */
public class AppleMusicPlugin implements DataPlugin {
    private static final String NAME = "AppleMusic";
    // set the path to your credentials.json file
    // private static final String CREDENTIALS_PATH = "/path/to/credentials.json";
    private static String CREDENTIALS_PATH = "src/main/java/edu/cmu/cs/cs214/credentials.json";
    private static final String APPLE_MUSIC_API_URL = "https://api.music.apple.com/v1/catalog/us/playlists/";
    private String playlistUrl = "https://music.apple.com/us/playlist/pop-playlist-2023/pl.8041a56e48ac4650aa0bb67aff6194c6";
    private String developerToken;
    private MelodyFramework framework;
    private LyricsFetcher lyricsFetcher;

    /**
     * Constructor for AppleMusicPlugin.
     */
    public AppleMusicPlugin() {
        lyricsFetcher = new LyricsFetcher();
        setupDeveloperToken();
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
     *
     * @param framework the framework that the plugin is registered to
     */
    @Override
    public void onRegister(MelodyFramework framework) {
        this.framework = framework;
    }

    /**
     * This method returns the developer token.
     *
     * @param isDefault whether the default developer token should be returned
     * @return the developer token
     */
    @Override
    public String getAccessToken(boolean isDefault) {
        return developerToken;
    }

    /**
     * This method fetch data from Apple Music.
     *
     * @param accessToken the accessToken to use Apple Music API
     * @return a list of tracks objects
     */
    @Override
    public List<Track> getData(String accessToken) {
        System.out.println("Fetching data from Apple Music API. Please wait...");

        String playlistId = extractPlaylistIdFromUrl(playlistUrl);
        List<Track> trackList = new ArrayList<>();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(APPLE_MUSIC_API_URL + playlistId)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
            JSONObject responseObject = new JSONObject(responseBody);
            JSONArray tracksArray = responseObject.getJSONArray("data")
                    .getJSONObject(0).getJSONObject("relationships")
                    .getJSONObject("tracks").getJSONArray("data");
            for (int i = 0; i < tracksArray.length(); i++) {
                JSONObject trackObject = tracksArray.getJSONObject(i).getJSONObject("attributes");
                String title = trackObject.getString("name");
                String artist = trackObject.getString("artistName");
                JSONArray genreArray = trackObject.getJSONArray("genreNames");
                List<String> genre = new ArrayList<>();
                for (int j = 0; j < genreArray.length(); j++) {
                    genre.add(genreArray.getString(j));
                }
                LocalDate localDate = LocalDate.parse(trackObject.getString("releaseDate"));
                Instant timeAdded = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
                String lyrics = lyricsFetcher.fetchLyrics(title, artist);
                double[] sentimentScore = framework.analyzeSentimentText(lyrics);

                Track customTrack = new Track(title, artist, genre, timeAdded.toString(), sentimentScore);
                trackList.add(customTrack);
            }
            return trackList;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error fetching data from Apple Music API.");
        }
        return trackList;
    }

    /**
     * This method set up access to Apple Music API.
     */
    private void setupDeveloperToken() {
        try {
            String credentials = new String(Files.readAllBytes(Paths.get(CREDENTIALS_PATH)));
            JSONObject credentialsJson = new JSONObject(credentials);
            String privateKeyString = credentialsJson.getString("appleMusicPrivateKey");
            String keyId = credentialsJson.getString("appleMusicKeyId");
            String teamId = credentialsJson.getString("appleMusicTeamId");

            // Convert the Base64-encoded private key string into an ECPrivateKey object
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyString);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            Algorithm algorithm = Algorithm.ECDSA256(null, (ECPrivateKey) privateKey);
            LocalDateTime now = LocalDateTime.now();
            Date issuedAt = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
            Date expiresAt = Date.from(now.plusMinutes(15).atZone(ZoneId.systemDefault()).toInstant());

            this.developerToken = JWT.create()
                    .withIssuer(teamId)
                    .withKeyId(keyId)
                    .withIssuedAt(issuedAt)
                    .withExpiresAt(expiresAt)
                    .sign(algorithm);

            System.out.println("Successfully set up Apple Music API with developer credentials.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error reading credentials file.");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println("Error setting up developer token - NoSuchAlgorithmException.");
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            System.out.println("Error setting up developer token - InvalidKeySpecException.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error setting up developer token.");
        }
    }

    /**
     * This method set the playlist URL to fetch.
     *
     * @return the playlist URL
     */
    public void setPlaylistUrl(String playlistUrl) {
        this.playlistUrl = playlistUrl;
    }

    /**
     * This method extract the playlist ID from the playlist URL.
     *
     * @param url the playlist URL
     * @return the playlist ID
     */
    private String extractPlaylistIdFromUrl(String url) {
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }

    /**
     * This main method shows what the fetched data from Apple Music API looks like.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Replace with your own playlist URL
        String playlistUrl = "https://music.apple.com/us/playlist/pop-playlist-2023/pl.8041a56e48ac4650aa0bb67aff6194c6";
        AppleMusicPlugin plugin = new AppleMusicPlugin();
        MelodyFramework melodyFramework = new MelodyFramework();
        plugin.onRegister(melodyFramework);
        plugin.setPlaylistUrl(playlistUrl);
        boolean isDefault = true;
        String accessToken = plugin.getAccessToken(isDefault);
        List<Track> tracks = plugin.getData(accessToken);

        System.out.println("Tracks from Apple Music playlist:");
        for (Track track : tracks) {
            System.out.println(track.toString());
        }
    }
}
