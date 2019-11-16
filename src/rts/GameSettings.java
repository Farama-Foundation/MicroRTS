package rts;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GameSettings {

    public enum LaunchMode {
        STANDALONE,
        GUI,
        SERVER,
        CLIENT
    }

    // Networking
    private String serverAddress = "";
    private int serverPort = 9898;
    private LaunchMode launchMode;

    private int serializationType = 1; // Default is JSON

    // Maps
    private String mapLocation = "";

    // Game settings
    private int maxCycles = 5000;
    private boolean partiallyObservable = false;
    private int uttVersion = 1;
    private int conflictPolicy = 1;
    
    // Opponents:
    private String AI1 = "";
    private String AI2 = "";
    

    public GameSettings( LaunchMode launchMode, String serverAddress, int serverPort, 
                          int serializationType, String mapLocation, int maxCycles, 
                          boolean partiallyObservable, int uttVersion, int confictPolicy, 
                          String AI1, String AI2) {
        this.launchMode = launchMode;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.serializationType = serializationType;
        this.mapLocation = mapLocation;
        this.maxCycles = maxCycles;
        this.partiallyObservable = partiallyObservable;
        this.uttVersion = uttVersion;
        this.conflictPolicy = confictPolicy;
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

    /**
     * Fetches the default configuration file which will be located in the root direction called "config.properties".
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

        String serverAddress = prop.getProperty("server_address");
        int serverPort = readIntegerProperty(prop, "server_port", 9898);
        int serializationType = readIntegerProperty(prop, "serialization_type", 2);
        String mapLocation = prop.getProperty("map_location");
        int maxCycles = readIntegerProperty(prop, "max_cycles", 5000);
        boolean partiallyObservable = Boolean.parseBoolean(prop.getProperty("partially_observable"));
        int uttVersion = readIntegerProperty(prop, "UTT_version", 2);
        int conflictPolicy = readIntegerProperty(prop, "conflict_policy", 1);
        LaunchMode launchMode = LaunchMode.valueOf(prop.getProperty("launch_mode"));
        String AI1 = prop.getProperty("AI1");
        String AI2 = prop.getProperty("AI2");

        return new GameSettings(launchMode, serverAddress, serverPort,
                                serializationType, mapLocation, maxCycles,
                                partiallyObservable, uttVersion, conflictPolicy, 
                                AI1, AI2);
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
