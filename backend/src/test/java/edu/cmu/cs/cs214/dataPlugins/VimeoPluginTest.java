package edu.cmu.cs.cs214.dataPlugins;

import edu.cmu.cs.cs214.core.MelodyFramework;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class VimeoPluginTest {

    private DataPlugin vimeoPlugin;
    private MelodyFramework melodyFramework;
    private String accessToken = "7e8546c3b00706c8d405c33406599d4a";

    @BeforeEach
    public void setUp() {
        melodyFramework = Mockito.mock(MelodyFramework.class);
        vimeoPlugin = new VimeoPlugin();
        vimeoPlugin.onRegister(melodyFramework);
    }

    @Test
    public void testGetName() {
        assertEquals("Vimeo", vimeoPlugin.getName());
    }

    @Test
    public void testGetData() {
        List<Track> tracks = vimeoPlugin.getData(accessToken);
        assertNotEquals(0, tracks.size());
    }
}
