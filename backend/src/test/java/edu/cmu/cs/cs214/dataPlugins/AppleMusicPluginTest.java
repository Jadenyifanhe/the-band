package edu.cmu.cs.cs214.dataPlugins;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import edu.cmu.cs.cs214.core.MelodyFramework;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for AppleMusicPlugin
 * Test might fail if the credentials path in corresponding data plugin is not set correctly
 */
public class AppleMusicPluginTest {
    private static final String TEST_PLAYLIST_URL = "https://music.apple.com/us/playlist/pop-playlist-2023/pl.8041a56e48ac4650aa0bb67aff6194c6";
    private AppleMusicPlugin plugin;
    private MelodyFramework melodyFramework;
    private String testAccessToken;
    boolean isDefault = true;

    @BeforeEach
    public void setUp() {
        melodyFramework = Mockito.mock(MelodyFramework.class);
        plugin = new AppleMusicPlugin();
        plugin.onRegister(melodyFramework);
        plugin.setPlaylistUrl(TEST_PLAYLIST_URL);
        testAccessToken = plugin.getAccessToken(isDefault);
    }

    @Test
    public void testGetName() {
        assertEquals("AppleMusic", plugin.getName());
    }

    @Test
    public void testGetData() {
        List<Track> tracks = plugin.getData(testAccessToken);
        assertNotNull(tracks);
    }

    @Test
    public void testExtractPlaylistIdFromUrl() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = AppleMusicPlugin.class.getDeclaredMethod("extractPlaylistIdFromUrl", String.class);
        method.setAccessible(true);

        String url = "https://music.apple.com/us/playlist/pop-playlist-2023/pl.8041a56e48ac4650aa0bb67aff6194c6";
        String expectedId = "pl.8041a56e48ac4650aa0bb67aff6194c6";
        String actualId = (String) method.invoke(plugin, url);
        assertEquals(expectedId, actualId, "The extracted playlist ID should match the expected value.");
    }
}
