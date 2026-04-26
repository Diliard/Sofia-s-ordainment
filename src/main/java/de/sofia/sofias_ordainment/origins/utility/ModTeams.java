package de.sofia.sofias_ordainment.origins.utility;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;

public class ModTeams {

    public static final String SOUL_STAIN_TEAM_5 = "soul_stain_glow_5";
    public static final String SOUL_STAIN_TEAM_10 = "soul_stain_glow_10";
    public static final String SCULKED_SIGHT = "sculked_sight";

    public static Team getOrCreate(ServerWorld world, String name, Formatting color) {
        Scoreboard scoreboard = world.getScoreboard();

        Team team = scoreboard.getTeam(name);
        if (team == null) {
            team = scoreboard.addTeam(name);
            team.setColor(color);
            team.setShowFriendlyInvisibles(false);
            team.setFriendlyFireAllowed(true);
        }

        return team;
    }
}
