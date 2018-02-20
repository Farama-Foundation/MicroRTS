package rts;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GameSettings {

    enum LaunchMode {
        SERVER,
        CLIENT,
        STANDALONE,
        TOURNAMENT
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
    private int rulesVersion = 1;
    private int conflictPolicy = 1;

    private GameSettings( LaunchMode launchMode, String serverAddress, int serverPort, int serializationType, String mapLocation, int maxCycles, boolean partiallyObservable, int rulesVersion, int confictPolicy) {
        this.launchMode = launchMode;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.serializationType = serializationType;
        this.mapLocation = mapLocation;
        this.maxCycles = maxCycles;
        this.partiallyObservable = partiallyObservable;
        this.rulesVersion = rulesVersion;
        this.conflictPolicy = confictPolicy;
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

    public int getRulesVersion() {
        return rulesVersion;
    }

    public int getConflictPolicy() {
        return conflictPolicy;
    }

    public LaunchMode getLaunchMode() {
        return launchMode;
    }

    /**
     * Fetches the default configuration file which will be located in the root direction called "config.properties".
     */
    public static Properties fetchDefaultConfig() throws IOException {
        Properties prop = new Properties();
        InputStream is = GameSettings.class.getResourceAsStream("/config.properties");
        prop.load(is);
        return prop;
    }

    /**
     * Generates game settings based on the provided configuration file.
     */
    public static GameSettings loadFromConfig(Properties prop) {

        assert !prop.isEmpty();

        String serverAddress = prop.getProperty("server_address");
        int serverPort = Integer.parseInt(prop.getProperty("server_port"));
        int serializationType = Integer.parseInt(prop.getProperty("serialization_type"));
        String mapLocation = prop.getProperty("map_location");
        int maxCycles = Integer.parseInt(prop.getProperty("max_cycles"));
        boolean partiallyObservable = Boolean.parseBoolean(prop.getProperty("partially_observable"));
        int rulesVersion = Integer.parseInt(prop.getProperty("rules_version"));
        int conflictPolicy = Integer.parseInt(prop.getProperty("conflict_policy"));
        LaunchMode launchMode = LaunchMode.valueOf(prop.getProperty("launch_mode"));

        return new GameSettings(launchMode,serverAddress,serverPort,serializationType,mapLocation,maxCycles,partiallyObservable,rulesVersion,conflictPolicy);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("----------Game Settings----------\n");
        sb.append("Running as Server: ").append( getLaunchMode().toString() ).append("\n");
        sb.append("Server Address: ").append( getServerAddress() ).append("\n");
        sb.append("Server Port: ").append( getServerPort() ).append("\n");
        sb.append("Serialization Type: ").append( getSerializationType()).append("\n");
        sb.append("Map Location: ").append( getMapLocation() ).append("\n");
        sb.append("Max Cycles: ").append( getMaxCycles() ).append("\n");
        sb.append("Partially Observable: ").append( isPartiallyObservable() ).append("\n");
        sb.append("Rules Version: ").append( getRulesVersion() ).append("\n");
        sb.append("Conflict Policy: ").append( getConflictPolicy() ).append("\n");
        sb.append("------------------------------------------------");
        return sb.toString();
    }
}
