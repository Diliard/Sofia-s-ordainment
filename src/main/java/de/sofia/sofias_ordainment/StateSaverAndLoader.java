package de.sofia.sofias_ordainment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StateSaverAndLoader extends PersistentState {
    public List<UUID> permSculked = new ArrayList<>();
    public List<UUID> sculkedSightSensorRelayDisabled = new ArrayList<>();

    private StateSaverAndLoader() {
    }

    private StateSaverAndLoader(List<UUID> permSculked, List<UUID> sculkedSightSensorRelayDisabled, List<UUID> legacyShriekerRelayDisabled) {
        this.permSculked = new ArrayList<>(permSculked);
        this.sculkedSightSensorRelayDisabled = new ArrayList<>(sculkedSightSensorRelayDisabled);
        for (UUID playerId : legacyShriekerRelayDisabled) {
            if (!this.sculkedSightSensorRelayDisabled.contains(playerId)) {
                this.sculkedSightSensorRelayDisabled.add(playerId);
            }
        }
    }

    private List<UUID> getPermSculked() {
        return permSculked;
    }

    private List<UUID> getSculkedSightSensorRelayDisabled() {
        return sculkedSightSensorRelayDisabled;
    }

    private List<UUID> getLegacyShriekerRelayDisabled() {
        return List.of();
    }

    public boolean isSculkedSightSensorRelayDisabled(UUID playerId) {
        return sculkedSightSensorRelayDisabled.contains(playerId);
    }

    public void setSculkedSightSensorRelayDisabled(UUID playerId, boolean disabled) {
        if (disabled) {
            if (!sculkedSightSensorRelayDisabled.contains(playerId)) {
                sculkedSightSensorRelayDisabled.add(playerId);
                markDirty();
            }
        } else if (sculkedSightSensorRelayDisabled.remove(playerId)) {
            markDirty();
        }
    }

    private static final Codec<StateSaverAndLoader> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                Uuids.CODEC.listOf().fieldOf("permSculked").forGetter(StateSaverAndLoader::getPermSculked),
                Uuids.CODEC.listOf().optionalFieldOf("sculkedSightSensorRelayDisabled", List.of()).forGetter(StateSaverAndLoader::getSculkedSightSensorRelayDisabled),
                Uuids.CODEC.listOf().optionalFieldOf("sculkedSightShriekerRelayDisabled", List.of()).forGetter(StateSaverAndLoader::getLegacyShriekerRelayDisabled)
        ).apply(instance, StateSaverAndLoader::new)
    );

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        return  persistentStateManager.getOrCreate(
                StateSaverAndLoader::createFromNbt,
                StateSaverAndLoader::new,
                "permsculked_data"
        );
    }

    public static StateSaverAndLoader createFromNbt(NbtCompound tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag)
                .resultOrPartial(System.err::println)
                .orElseGet(StateSaverAndLoader::new);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        CODEC.encodeStart(NbtOps.INSTANCE, this)
                .resultOrPartial(System.err::println)
                .ifPresent(encodedTag -> {
                    // We cast to NbtCompound because our Codec outputs a compound
                    nbt.copyFrom((NbtCompound) encodedTag);
                });
        return nbt;
    }
}
