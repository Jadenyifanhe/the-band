package edu.cmu.cs.cs214.dataPlugins;

import java.util.List;

import edu.cmu.cs.cs214.core.MelodyFramework;

/**
 * Data Plugin interface
 * The interface gets media data from multiple source.
 * Currently support plugins including Spotify, Apple Music and Vimeo.
 */
public interface DataPlugin {
    /**
     * @return The name of this DataPlugin
     *         e.g. For the Spotify plugin, return "spotify".
     */
    String getName();

    /**
     * Given accessToken sent from the frontend,
     * this method will get the data from the source API and transform the data into
     * a List<Track>.
     * 
     * @param accessToken The access token of the user for the data source.
     */
    List<Track> getData(String accessToken);

    /**
     * Get the access token of the user.
     *
     * @param isDefault Whether the access token is the default one.
     *                  If the access token is not the default one, the user will
     *                  get it from the browser.
     *
     * @return The access token of the user in a limit interval.
     */
    String getAccessToken(boolean isDefault);

    /**
     * Called (only once) when the plugin is first registered with the
     * framework, giving the plugin a chance to perform any initial set-up
     * before the analysis has begun (if necessary).
     *
     * @param framework The {@link MelodyFramework} instance with which the plug-in
     *                  was registered.
     */
    void onRegister(MelodyFramework framework);
}