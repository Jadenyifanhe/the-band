package edu.cmu.cs.cs214.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;

import edu.cmu.cs.cs214.dataPlugins.DataPlugin;

public class MelodyFrameworkTest {

    private MelodyFramework melodyFramework;

    @BeforeEach
    public void setUp() {
        melodyFramework = new MelodyFramework();
    }

    @Test
    public void testGetCurrentStage() {
        assertEquals(Stage.SELECT_DATA_PLUGIN, melodyFramework.getCurrentStage());
    }

    @Test
    public void testRegisterDataPlugin() {
        DataPlugin dataPlugin = Mockito.mock(DataPlugin.class);
        melodyFramework.registerDataPlugin(dataPlugin);
        assertEquals(1, melodyFramework.getNumberOfPlugins());

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            melodyFramework.registerDataPlugin(dataPlugin);
        });

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            melodyFramework.registerDataPlugin(null);
        });
    }

    @Test void testGetRegisteredDataPluginName() {
        DataPlugin dataPlugin = Mockito.mock(DataPlugin.class);
        Mockito.when(dataPlugin.getName()).thenReturn("testPlugin");

        melodyFramework.registerDataPlugin(dataPlugin);
        assertEquals("testPlugin", melodyFramework.getRegisteredDataPluginName().get(0));
    }

    @Test
    public void testNewMelody() {
        melodyFramework = new MelodyFramework();
        assertEquals(Stage.SELECT_DATA_PLUGIN, melodyFramework.getCurrentStage());
    }

    @Test
    public void testFrameworkWholeProcess() {
        DataPlugin dataPlugin = Mockito.mock(DataPlugin.class);
        melodyFramework.registerDataPlugin(dataPlugin);

        assertEquals(Stage.SELECT_DATA_PLUGIN, melodyFramework.getCurrentStage());
        melodyFramework.selectDataPlugin(dataPlugin);

        assertEquals(Stage.ENTER_USER_ACCESS_TOKEN, melodyFramework.getCurrentStage());
        Mockito.when(dataPlugin.getAccessToken(true)).thenReturn("accessToken");
        melodyFramework.setAccessToken("default", "");

        assertEquals(Stage.SELECT_DISPLAY_PLUGIN, melodyFramework.getCurrentStage());
        Mockito.when(dataPlugin.getData("accessToken")).thenReturn(new ArrayList<>());
        melodyFramework.getDataFromPlugin();

        assertEquals(Stage.DISPLAY, melodyFramework.getCurrentStage());
    }

}
