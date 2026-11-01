package pl.kuba6000.gtnhextranerf;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static volatile boolean telemetryEnabled = true;
    /** Team tracking is a separate opt-in feature; these values live in the configuration file. */
    public static volatile boolean teamTrackingEnabled = false;
    public static volatile String teamId = "";
    public static volatile String teamLeaderId = "";
    public static volatile String teamLeaderName = "";
    private static Configuration current;
    private static File currentFile;

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);
        telemetryEnabled = configuration.getBoolean(
            "enabled",
            "telemetry",
            true,
            "Send a random server-run ID, online player count, highest available world progression tier, singleplayer status and optional JAR proof once per hour. "
                + "We'd appreciate you keeping telemetry enabled. It is used only for community statistics about active servers, online players, and world progression. "
                + "Set false to disable all telemetry connections. Applies to the logical server, including singleplayer. "
                + "Privacy policy: https://extranerf.kuba6000.pl/privacy");
        teamTrackingEnabled = configuration.getBoolean(
            "enabled",
            "team_tracking",
            false,
            "Opt-in. Lists this server as a team on the Extra Nerf website: a permanent team ID, the name and UUID of the team leader and the names and UUIDs of the players who are online, every 15 minutes. "
                + "Independent of telemetry. Turn it on or off in game with /extranerfteam on|off.");
        teamId = configuration.getString(
            "id",
            "team_tracking",
            "",
            "Permanent team ID, created when team tracking is first enabled. Do not change it.");
        teamLeaderId = configuration.getString(
            "leader",
            "team_tracking",
            "",
            "UUID of the player whose name is the team name (the player who enabled team tracking).");
        teamLeaderName = configuration
            .getString("leaderName", "team_tracking", "", "Last known name of the team leader.");
        current = configuration;
        currentFile = configFile;

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    /** Owner command, called on the server thread. The decision also applies after restarting. */
    public static void setTelemetryEnabled(boolean enabled) {
        if (current == null) throw new IllegalStateException("Configuration has not been initialized");
        telemetryEnabled = enabled;
        current.get("telemetry", "enabled", true)
            .set(enabled);
        current.save();
    }

    /** Opt-in command, called on the server thread. The team ID is created once and then kept forever. */
    public static void enableTeamTracking(String newTeamId, String leaderId, String leaderName) {
        if (current == null) throw new IllegalStateException("Configuration has not been initialized");
        teamTrackingEnabled = true;
        teamId = newTeamId;
        teamLeaderId = leaderId;
        teamLeaderName = leaderName;
        current.get("team_tracking", "enabled", false)
            .set(true);
        current.get("team_tracking", "id", "")
            .set(newTeamId);
        current.get("team_tracking", "leader", "")
            .set(leaderId);
        current.get("team_tracking", "leaderName", "")
            .set(leaderName);
        current.save();
    }

    public static void disableTeamTracking() {
        if (current == null) throw new IllegalStateException("Configuration has not been initialized");
        teamTrackingEnabled = false;
        current.get("team_tracking", "enabled", false)
            .set(false);
        current.save();
    }

    /** Keeps the team name current when the leader changes their Minecraft name. */
    public static void updateTeamLeaderName(String leaderName) {
        if (current == null) throw new IllegalStateException("Configuration has not been initialized");
        teamLeaderName = leaderName;
        current.get("team_tracking", "leaderName", "")
            .set(leaderName);
        current.save();
    }

    public static File configurationFile() {
        if (currentFile == null) throw new IllegalStateException("Configuration has not been initialized");
        return currentFile;
    }
}
