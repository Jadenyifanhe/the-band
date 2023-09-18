package edu.cmu.cs.cs214.dataPlugins;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.json.JSONObject;

import edu.cmu.cs.cs214.core.MelodyFramework;

/**
 * This class is a data plugin that fetches data from Vimeo video API.
 */
public class VimeoPlugin implements DataPlugin {

    private static String CREDENTIALS_PATH = "src/main/java/edu/cmu/cs/cs214/credentials.json";
    private static final String API_BASE_URL = "https://api.vimeo.com";
    private static final String TOKEN_ENDPOINT = "/oauth/authorize?response_type=token&";
    private static final String REDIRECT_URL = "http://localhost:3000/";
    private static final String STATE = "0";
    private static final String USER_LIKES_ENDPOINT = "/users/198445417/likes";
    private static final int TIME_LENGTH = 19;
    private JSONObject vimeoCredentials;

    private MelodyFramework framework;

    public VimeoPlugin() {
        try {
            String credentials = new String(Files.readAllBytes(Paths.get(CREDENTIALS_PATH)));
            vimeoCredentials = new JSONObject(credentials);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error reading Spotify API credentials.");
        }
    }

    //yueflipped@gmail.com
    

    private String getUserLikes(String accessToken) throws IOException {
        // Construct the API endpoint URL
        String endpointUrl = API_BASE_URL + USER_LIKES_ENDPOINT;
        URL url = new URL(endpointUrl);

        // Open a connection to the API endpoint
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Set the request headers
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        connection.setRequestProperty("Accept", "application/vnd.vimeo.*+json;version=3.4");

        // Send the request and read the response
        int responseCode = connection.getResponseCode();
        String responseMessage = connection.getResponseMessage();
        if (responseCode != 200) {
            throw new RuntimeException("Request failed: " + responseCode + " " + responseMessage);
        }

        InputStream inputStream = connection.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        StringBuilder responseContent = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            responseContent.append(line);
        }

        // Close the connection
        connection.disconnect();

        return responseContent.toString();
    }

    /**
     * Get the name of the plugin
     */
    @Override
    public String getName() {
        return "Vimeo";
    }

    /**
     * Get the access token of the user from the browser.
     *
     * @param isDefault Whether the access token is the default one.
     * If the access token is not the default one, the user will get it from the browser.
     *
     * @return The access token of the user in a limit interval.
     */
    @Override
    public String getAccessToken(boolean isDefault) {
        if (isDefault) {
            return vimeoCredentials.getString("vimeoDefaultAccessToken");
        }

        String redirectURL = "";
        try (Playwright playwright = Playwright.create()) {
            BrowserType browserType = playwright.firefox();
            Browser browser = browserType.launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();

            // Navigate to the initial URL
            String authURL = API_BASE_URL + TOKEN_ENDPOINT + "client_id=" + vimeoCredentials.getString("vimeoClientId") + "&redirect_uri=" + REDIRECT_URL + "&state=" + STATE;
            page.navigate(authURL);

            // Wait for the page to redirect
            page.waitForURL(Pattern.compile("access_token="), new Page.WaitForURLOptions().setTimeout(60000));

            // After the user has manipulated the web page and any redirects have occurred,
            // obtain the final URL using the following method:
            redirectURL = page.url();

            System.out.println("Redirect URL: " + redirectURL);

            // Close the browser
            browser.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        // Parse the redirect URL to get the access token
        String accessTokenParam = "access_token=";
        String accessToken = "";

        int startIndex = redirectURL.indexOf(accessTokenParam);
        int endIndex = redirectURL.indexOf('&');

        if (startIndex != -1 && endIndex != -1) {
            accessToken = redirectURL.substring(startIndex + accessTokenParam.length(), endIndex);
            System.out.println("Access Token: " + accessToken);
        }

        return accessToken;
    }

    /**
     * The callback function when the plugin is registered to the framework.
     *
     * @param framework The framework that the plugin is registered to be used.
     */
    @Override
    public void onRegister(MelodyFramework framework) {
        this.framework = framework;
    }

    /**
     * Get the data from the plugin.
     * For Vimeo, the data is the user's liked videos.
     *
     * @param accessToken The access token of the user.
     * @return The list of tracks which records the user's liked videos.
     */
    @Override
    public List<Track> getData(String accessToken) {
        String responseContent = "";
        try {
            responseContent = getUserLikes(accessToken);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return new ArrayList<>();
        }

        List<Track> trackData = new ArrayList<>();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseContent).get("data");

             // Extract fields for each item
             for (JsonNode itemNode : rootNode) {
                String name = itemNode.get("name").asText();
                String artist = itemNode.get("user").get("name").asText();
                String description = itemNode.get("description").asText();
                String addedTime = itemNode.get("metadata")
                                           .get("interactions")
                                           .get("like")
                                           .get("added_time").asText().substring(0, TIME_LENGTH);

                List<String> categories = new ArrayList<>();
                for (JsonNode categoryNode : itemNode.get("categories")) {
                    categories.add(categoryNode.get("name").asText());
                }

                // Analyze the sentiment of the description of the video
                double[] score = framework.analyzeSentimentText(description);
                trackData.add(new Track(name, artist, categories, addedTime, score));
            }

            return trackData;
        }
        catch (JsonMappingException e) {
            System.err.println(e.getMessage());
        }
        catch (JsonProcessingException e) {
            System.err.println(e.getMessage());
        }

        return trackData;
    }
}