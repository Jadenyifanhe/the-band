package edu.cmu.cs.cs214.core;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import edu.cmu.cs.cs214.dataPlugins.Track;
import edu.cmu.cs.cs214.dataPlugins.DataPlugin;

/**
 * The framework for the Melody analysis. It is responsible for managing the data plugins and passing the
 * data to the display plugins .
 */
public class MelodyFramework {

    /**
     * The current data plugin.
     */
    private DataPlugin currentDataPlugin;

    /**
     * The list of registered data plugins.
     */
    private List<DataPlugin> registeredDataPlugins;

    /**
     * The list of tracks to be displayed.
     */
    private Track[] data;

    /**
     * The current stage of the framework.
     */
    private Stage currentStage;

    /**
     * The user access token.
     */
    private String accessToken;

    /**
     * Move to the next stage.
     *
     * @param stage The current stage.
     * @return The next stage.
     */
    private Stage nextStage(Stage stage) {
        switch (stage) {
            case SELECT_DATA_PLUGIN:
                return Stage.ENTER_USER_ACCESS_TOKEN;
            case ENTER_USER_ACCESS_TOKEN:
                return Stage.SELECT_DISPLAY_PLUGIN;
            case SELECT_DISPLAY_PLUGIN:
                return Stage.DISPLAY;
            default:
                return Stage.SELECT_DATA_PLUGIN;
        }
    }

    public MelodyFramework() {
        this.registeredDataPlugins = new ArrayList<>();
        this.currentDataPlugin = null;
        this.data = null;
        this.currentStage = Stage.SELECT_DATA_PLUGIN;
    }

    public Track[] getData() {
        return this.data;
    }

    public Stage getCurrentStage() {
        return this.currentStage;
    }

    public int getNumberOfPlugins() {
        return this.registeredDataPlugins.size();
    }

    /**
     * Registers a new {@link DataPlugin} with the game framework
     *
     * @param plugin The {@link DataPlugin} to be registered.
     */
    public void registerDataPlugin(DataPlugin plugin) throws IllegalArgumentException {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        if (this.registeredDataPlugins.contains(plugin)) {
            throw new IllegalArgumentException("Plugin already registered");
        }
        plugin.onRegister(this);
        this.registeredDataPlugins.add(plugin);
    }

    /**
     * Selects a {@link DataPlugin} to be used for the current analysis.
     *
     * @param Plugin The {@link DataPlugin} to be used.
     */
    public void selectDataPlugin(DataPlugin plugin) {
        this.currentDataPlugin = plugin;
        this.currentStage = nextStage(this.currentStage);
    }

    /**
     * Gets the names of all registered {@link DataPlugin}s.
     *
     * @return A list of names of all registered {@link DataPlugin}s.
     */
    public List<String> getRegisteredDataPluginName() {
        return registeredDataPlugins.stream().map(DataPlugin::getName).collect(Collectors.toList());
    }

    public void newMelody() {
        // this.registeredDataPlugins = new ArrayList<>();
        this.currentDataPlugin = null;
        this.data = null;
        this.currentStage = Stage.SELECT_DATA_PLUGIN;
    }

    /**
     * Gets the data from the selected {@link DataPlugin}.
     */
    public void getDataFromPlugin() {
        List<Track> tracks = currentDataPlugin.getData(accessToken);
        this.data = Arrays.copyOf(tracks.toArray(new Track[0]), tracks.size());
        System.out.println(this.currentStage);
        this.currentStage = nextStage(this.currentStage);
        System.out.println(this.currentStage);
    }

    /**
     * Set the access token for the current {@link DataPlugin}.
     *
     * @param method      The method to get the access token. It can be "custom",
     *                    "default" or "browser".
     *                    If method is "custom", then the access token is the one
     *                    got from the frontend.
     * @param accessToken The access token got from the user.
     */
    public void setAccessToken(String method, String accessToken) {
        if (method.equals("custom")) {
            this.accessToken = accessToken;
        } else if (method.equals("default")) {
            this.accessToken = currentDataPlugin.getAccessToken(true);
        } else { // method = "browser"
            this.accessToken = currentDataPlugin.getAccessToken(false);
        }
        this.currentStage = nextStage(this.currentStage);
    }

    /**
     * Compute the sentiment score of the given text.
     *
     * @param text The text to be analyzed. Usually it is the lyrics of a song
     *             or the description of the video.
     *
     * @return The sentiment score of the given text.
     */
    public double[] analyzeSentimentText(String text) {
        try {
            String jsonKey = new String(
                    Files.readAllBytes(Paths.get("src/main/java/edu/cmu/cs/cs214/sentimentConfig.json")),
                    StandardCharsets.UTF_8);
            return Analyze.analyzeSentimentText(text, jsonKey);
        } catch (IOException e) {
            System.out.println("Error reading JSON key file: " + e.getMessage());
        }
        return new double[] { 0, 0 };
    }

}
