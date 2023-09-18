package edu.cmu.cs.cs214.dataPlugins;

import edu.cmu.cs.cs214.core.MelodyFramework;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class for SpotifyPlugin
 * Test might fail if the credentials path in corresponding data plugin is not set correctly
 */
public class SpotifyPluginTest {
    private static final String TEST_PLAYLIST_URL = "https://open.spotify.com/playlist/37i9dQZF1EQncLwOalG3K7";
    private SpotifyPlugin spotifyPlugin;
    private MelodyFramework melodyFramework;
    private String testAccessToken;
    boolean isDefault = true;

    @BeforeEach
    public void setUp() {
        melodyFramework = Mockito.mock(MelodyFramework.class);
        spotifyPlugin = new SpotifyPlugin();
        spotifyPlugin.onRegister(melodyFramework);
        spotifyPlugin.setPlaylistUrl(TEST_PLAYLIST_URL);
        testAccessToken = spotifyPlugin.getAccessToken(isDefault);
    }

    @Test
    public void testGetName() {
        assertEquals("Spotify", spotifyPlugin.getName());
    }

    @Test
    public void testGetData() {
        List<Track> tracks = spotifyPlugin.getData(testAccessToken);
        assertNotNull(tracks);
    }
}
