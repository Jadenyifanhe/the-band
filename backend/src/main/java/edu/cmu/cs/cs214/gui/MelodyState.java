package edu.cmu.cs.cs214.gui;

import java.util.Arrays;
import java.util.List;

import edu.cmu.cs.cs214.dataPlugins.Track;
import edu.cmu.cs.cs214.core.MelodyFramework;
import edu.cmu.cs.cs214.core.Stage;

public class MelodyState {

    private final Track[] trackData;
    private final Plugin[] plugins;
    private final Stage stage;

    private MelodyState(Track[] trackData, Plugin[] plugins, Stage stage) {
        this.trackData = trackData;
        this.plugins = plugins;
        this.stage = stage;
    }

    public static MelodyState forMelody(MelodyFramework framework) {
        Plugin[] plugins = getPlugins(framework);

        return new MelodyState(framework.getData(), plugins, framework.getCurrentStage());
    }

    private static Plugin[] getPlugins(MelodyFramework framework) {
        List<String> dataPlugins = framework.getRegisteredDataPluginName();
        System.out.println("plugin list: " + dataPlugins.size());

        Plugin[] plugins = new Plugin[dataPlugins.size()];
        for (int i = 0; i < dataPlugins.size(); i++) {
            plugins[i] = new Plugin(dataPlugins.get(i));
            System.out.println(plugins[i]);
        }
        return plugins;
    }

    @Override
    public String toString() {
        return ("{ \"trackData\": " + Arrays.toString(trackData) + "," +
                " \"plugins\": " + Arrays.toString(plugins) + "," +
                " \"stage\": \"" + this.stage.toString() + "\"}").replace("null", "[]");
    }

}
