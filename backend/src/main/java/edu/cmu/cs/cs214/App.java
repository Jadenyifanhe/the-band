package edu.cmu.cs.cs214;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import fi.iki.elonen.NanoHTTPD;

import edu.cmu.cs.cs214.core.MelodyFramework;
import edu.cmu.cs.cs214.dataPlugins.DataPlugin;
import edu.cmu.cs.cs214.gui.MelodyState;

public class App extends NanoHTTPD {

    public static void main(String[] args) {
        try {
            new App();
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        }
    }

    private MelodyFramework framework;
    private List<DataPlugin> plugins;

    /**
     * Start the server at :8080 port.
     *
     * @throws IOException
     */
    public App() throws IOException {
        super(8080);

        this.framework = new MelodyFramework();
        plugins = loadPlugins();
        for (DataPlugin p : plugins) {
            try {
                framework.registerDataPlugin(p);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }

        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Map<String, String> params = session.getParms();
        if (uri.equals("/newMelody")) {
            framework.newMelody();
        } else if (uri.equals("/dataPlugin")) {
            framework.selectDataPlugin(plugins.get(Integer.parseInt(params.get("i"))));
        } else if (uri.equals("/getAccessToken")) {
            String method = params.get("method");
            String token = params.get("token");
            framework.setAccessToken(method, token);
        } else if (uri.equals("/showDisplay")) {
            framework.getDataFromPlugin();
        }

        MelodyState play = MelodyState.forMelody(this.framework);
        System.out.println(play.toString());
        return newFixedLengthResponse(play.toString());

    }

    /**
     * Load plugins listed in META-INF/services/...
     *
     * @return List of instantiated plugins
     */
    private static List<DataPlugin> loadPlugins() {
        ServiceLoader<DataPlugin> plugins = ServiceLoader.load(DataPlugin.class);
        List<DataPlugin> result = new ArrayList<>();
        for (DataPlugin plugin : plugins) {
            System.out.println("Loaded plugin " + plugin.getName());
            result.add(plugin);
        }
        return result;
    }
}
