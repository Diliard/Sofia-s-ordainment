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

    private StateSaverAndLoader() {
    }

    private StateSaverAndLoader(List<UUID> permSculked) {
        this.permSculked = new ArrayList<>(permSculked);
    }

    private List<UUID> getPermSculked() {
        return permSculked;
    }

    private static final Codec<StateSaverAndLoader> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                Uuids.CODEC.listOf().fieldOf("permSculked").forGetter(StateSaverAndLoader::getPermSculked)
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
