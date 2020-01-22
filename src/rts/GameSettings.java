package rts;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GameSettings {

    static String getHelpMessage() {
        return "-f: path to config file\n" +
                "-s: server IP address\n" +
                "-p: server port\n" +
                "-l: launch mode (STANDALONE, GUI, SERVER, CLIENT)\n" +
                "--serialization: serialization type (1 for XML, 2 for JSON)\n" +
                "-m: path for the map file\n" +
                "-c: max cycles\n" +
                "-i: update interval between each tick, in milliseconds\n" +
                "--headless: 1 or true, 0 or false\n" +
                "--partially_observable: 1 or true, 0 or false\n" +
                "-u: unit type table version\n" +
                "--conflict_policy: which conflict policy to use\n" +
                "--ai1: name of the class to be instantiated for player 1\n" +
                "--ai2: name of the class to be instantiated for player 2";
    }

    public enum LaunchMode {
        STANDALONE,
        HUMAN,
        GUI,
        SERVER,
        CLIENT
    }

    // Networking
    private String serverAddress = "localhost";
    private int serverPort = 9898;
    private LaunchMode launchMode = LaunchMode.GUI;

    private int serializationType = 2; // Default is JSON

    // Maps
    private String mapLocation = "";

    // Game settings
    private int maxCycles = 5000;
    private int updateInterval = 20;
    private boolean partiallyObservable = false;
    private int uttVersion = 1;
    private int conflictPolicy = 1;

    private boolean includeConstantsInState = true, compressTerrain = false;
    private boolean headless = false;

    // Opponents:
    private String AI2 = "ai.RandomAI";
    private String AI1 = "ai.RandomAI";

    public GameSettings(LaunchMode launchMode, String serverAddress, int serverPort,
        int serializationType, String mapLocation, int maxCycles, int updateInterval,
        boolean partiallyObservable, int uttVersion, int confictPolicy,
        boolean includeConstantsInState, boolean compressTerrain, boolean headless, String AI1,
        String AI2) {
        this.launchMode = launchMode;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.serializationType = serializationType;
        this.mapLocation = mapLocation;
        this.maxCycles = maxCycles;
        this.updateInterval = updateInterval;
        this.partiallyObservable = partiallyObservable;
        this.uttVersion = uttVersion;
        this.conflictPolicy = confictPolicy;
        this.includeConstantsInState = includeConstantsInState;
        this.compressTerrain = compressTerrain;
        this.headless = headless;
        this.AI1 = AI1;
        this.AI2 = AI2;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getSerializationType() {
        return serializationType;
    }

    public String getMapLocation() {
        return mapLocation;
    }

    public int getMaxCycles() {
        return maxCycles;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public boolean isPartiallyObservable() {
        return partiallyObservable;
    }

    public int getUTTVersion() {
        return uttVersion;
    }

    public int getConflictPolicy() {
        return conflictPolicy;
    }

    public LaunchMode getLaunchMode() {
        return launchMode;
    }

    public String getAI1() {
        return AI1;
    }

    public String getAI2() {
        return AI2;
    }

    public boolean isIncludeConstantsInState() {
        return includeConstantsInState;
    }

    public boolean isCompressTerrain() {
        return compressTerrain;
    }

    public boolean isHeadless() {
        return headless;
    }

    /**
     * Create a GameSettings object from a list of command-line arguments
     * @param args a String array containing command-line arguments
     */
    public GameSettings(String[] args) {
        overrideFromArgs(args);
    }

    /**
     * Use a list of command-line arguments to replace the attribute values of the current
     * GameSettings
     *
     * -s: server IP address
     * -p: server port
     * -l: launch mode (see @launchMode)
     * --serialization: serialization type (1 for XML, 2 for JSON)
     * -m: path for the map file
     * -c: max cycles
     * -i: update interval between each tick, in milliseconds
     * --headless: headless: 1 or true, 0 or false
     * --partially_observable: 1 or true, 0 or false
     * -u: unit type table version
     * --conflict_policy: which conflict policy to use
     * --ai1: name of the class to be instantiated for player 1
     * --ai2: name of the class to be instantiated for player 2
     *
     * @param args a String array containing command-line arguments
     * @return
     */
    public GameSettings overrideFromArgs(String[] args) {
        for (int i = args.length; i > 0; i--) {
            switch (args[i - 1]) {
                case "-s":
                    serverAddress = args[i];
                    break;
                case "-p":
                    serverPort = Integer.parseInt(args[i]);
                    break;
                case "-l":
                    launchMode = LaunchMode.valueOf(args[i]);
                    break;
                case "--serialization":
                    serializationType = Integer.parseInt(args[i]);
                    break;
                case "-m":
                    mapLocation = args[i];
                    break;
                case "-c":
                    maxCycles = Integer.parseInt(args[i]);
                    break;
                case "-i":
                    updateInterval = Integer.parseInt(args[i]);
                    break;
                case "--headless":
                    headless = args[i].equals("1") || Boolean.parseBoolean(args[i]);
                    break;
                case "--partially_observable":
                    partiallyObservable = args[i].equals("1") || Boolean.parseBoolean(args[i]);
                    break;
                case "-u":
                    uttVersion = Integer.parseInt(args[i]);
                    break;
                case "--conflict_policy":
                    conflictPolicy = Integer.parseInt(args[i]);
                    break;
                case "--ai1":
                    AI1 = args[i];
                    break;
                case "--ai2":
                    AI2 = args[i];
                    break;
                default:
                    break;
            }
        }
        return this;
    }

    /**
     * Fetches the default configuration file which will be located in the root direction called
     * "config.properties".
     */
    public static Properties fetchDefaultConfig() throws IOException {
        Properties prop = new Properties();
        InputStream is = GameSettings.class.getResourceAsStream("/config.properties");
        if (is == null) is = new FileInputStream("resources/config.properties");
        prop.load(is);
        return prop;
    }
   
     /**
     * Fetches the configuration file which will be located in the path on propertiesFile.
     */
    public static Properties fetchConfig(String propertiesFile) throws IOException {
        Properties prop = new Properties();
        InputStream is = GameSettings.class.getResourceAsStream(propertiesFile);
        if (is == null) is = new FileInputStream(propertiesFile);
        prop.load(is);
        return prop;
    }
    
    /**
     * Generates game settings based on the provided configuration file.
     */
    public static GameSettings loadFromConfig(Properties prop) {

        assert !prop.isEmpty();

        String serverAddress = prop.getProperty("server_address", "localhost");
        int serverPort = readIntegerProperty(prop, "server_port", 9898);
        int serializationType = readIntegerProperty(prop, "serialization_type", 2);
        String mapLocation = prop.getProperty("map_location");
        int maxCycles = readIntegerProperty(prop, "max_cycles", 5000);
        int updateInterval = readIntegerProperty(prop, "update_interval", 20);
        boolean partiallyObservable = Boolean.parseBoolean(prop.getProperty("partially_observable"));
        int uttVersion = readIntegerProperty(prop, "UTT_version", 2);
        int conflictPolicy = readIntegerProperty(prop, "conflict_policy", 1);
        LaunchMode launchMode = LaunchMode.valueOf(prop.getProperty("launch_mode",  "GUI"));
        boolean includeConstantsInState = Boolean.parseBoolean(prop.getProperty("constants_in_state", "true"));
        boolean compressTerrain = Boolean.parseBoolean(prop.getProperty("compress_terrain", "false"));
        boolean headless = Boolean.parseBoolean(prop.getProperty("headless", "false"));
        String AI1 = prop.getProperty("AI1", "ai.RandomAI");
        String AI2 = prop.getProperty("AI2", "ai.RandomAI");

        return new GameSettings(launchMode, serverAddress, serverPort, serializationType,
            mapLocation, maxCycles, updateInterval, partiallyObservable, uttVersion, conflictPolicy,
            includeConstantsInState, compressTerrain, headless, AI1, AI2);
    }
    
    
    public static int readIntegerProperty(Properties prop, String name, int defaultValue)
    {
        String stringValue = prop.getProperty(name);
        if (stringValue == null) return defaultValue;
        return Integer.parseInt(stringValue);
    }
    

    @Override
    public String toString() {
        return "----------Game Settings----------\n" +
                "Running as Server: " + getLaunchMode().toString() + "\n" +
                "Server Address: " + getServerAddress() + "\n" +
                "Server Port: " + getServerPort() + "\n" +
                "Serialization Type: " + getSerializationType() + "\n" +
                "Map Location: " + getMapLocation() + "\n" +
                "Max Cycles: " + getMaxCycles() + "\n" +
                "Partially Observable: " + isPartiallyObservable() + "\n" +
                "Rules Version: " + getUTTVersion() + "\n" +
                "Conflict Policy: " + getConflictPolicy() + "\n" +
                "AI1: " + getAI1() + "\n" +
                "AI2: " + getAI2() + "\n" +
                "------------------------------------------------";
    }
}
