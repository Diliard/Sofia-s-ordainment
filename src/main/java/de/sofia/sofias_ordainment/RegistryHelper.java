package de.sofia.sofias_ordainment;

import de.sofia.sofias_ordainment.effects.Sculkified;
import de.sofia.sofias_ordainment.entities.CustomPotionEntity;
import de.sofia.sofias_ordainment.origins.abstractPowers.AbstractOnHitPower;
import de.sofia.sofias_ordainment.origins.powers.*;
import de.sofia.sofias_ordainment.origins.utility.SculkifiedSourceTrackerHelper;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.CooldownPower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import static de.sofia.sofias_ordainment.Sofias_ordainment.MOD_ID;

public class RegistryHelper {
    public static final StatusEffect SCULKIFIED = register(new Sculkified(), "sculkified");

    private static StatusEffect register(StatusEffect statusEffect, String id) {
        // Create the identifier for the item.
        Identifier itemID = Identifier.of(MOD_ID, id);

        // Return the registered item!
        return Registry.register(Registries.STATUS_EFFECT, itemID, statusEffect);
    }

    public static final PowerFactory<Power> SOUL_CONVERSION =
            new PowerFactory<>(
                    new Identifier("sofias_ordainment", "soul_conversion"),
                    new SerializableData(),
                    data -> (type, player) -> new Soul_Conversion(type, player)
            ).allowCondition();

    public static final PowerFactory<Power> SOUL_STAIN =
            new PowerFactory<>(
                    new Identifier("sofias_ordainment", "soul_stain"),
                    new SerializableData(),
                    data -> (type, player) -> new Soul_Stain(type, player)
            ).allowCondition();

    public static final PowerFactory<Power> SUCCULENCE =
            new PowerFactory<>(
                    new Identifier("sofias_ordainment", "succulence"),
                    new SerializableData(),
                    data -> (type, player) -> new Succulence(type, player)
            ).allowCondition();

    public static final PowerFactory<?> SCULKED_SIGHT =
        new PowerFactory<>(
                new Identifier(MOD_ID, "sculked_sight"),
                new SerializableData(),
                data -> (type, player) -> new Sculked_Sight(
                        type,
                        player
                )
        ).allowCondition();

    public static final PowerFactory<Power> DISABLE_CRIT =
            new PowerFactory<>(
                    new Identifier("sofias_ordainment", "sculked_precision"),
                    new SerializableData(),
                    data -> (type, player) -> new DisableCritPower(type, player)
            ).allowCondition();

    public static final HudRender SCREECH_HUD = new HudRender(
            true,
            1,
            new Identifier(MOD_ID, "textures/gui/resource_bar.png"),
            null,
            false
    );

    public static final HudRender GROUNDING_HUD = new HudRender(
            true,
            2,
            new Identifier(MOD_ID, "textures/gui/resource_bar.png"),
            null,
            false
    );

    public static final PowerFactory<CooldownPower> SCREECH =
            new PowerFactory<CooldownPower>(
                    new Identifier("sofias_ordainment", "screech"),
                    new SerializableData()
                            .add("cooldown", SerializableDataTypes.INT)
                            .add("hud_render", ApoliDataTypes.HUD_RENDER, SCREECH_HUD)
                            .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
                    data -> (type, player) -> {
                        Screech power = new Screech(type, player, data.getInt("cooldown"), data.get("hud_render"));
                        power.setKey(data.get("key"));
                        return power;
                    }
            ).allowCondition();

    public static final PowerFactory<CooldownPower> GROUNDING =
            new PowerFactory<CooldownPower>(
                    new Identifier("sofias_ordainment", "grounding"),
                    new SerializableData()
                            .add("cooldown", SerializableDataTypes.INT)
                            .add("hud_render", ApoliDataTypes.HUD_RENDER, GROUNDING_HUD)
                            .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
                    data -> (type, player) -> {
                        Grounding power = new Grounding(type, player, data.getInt("cooldown"), data.get("hud_render"));
                        power.setKey(data.get("key"));
                        return power;
                    }
            ).allowCondition();

    public static final EntityType<CustomPotionEntity> CUSTOM_POTION_ENTITY =
            Registry.register(
                    Registries.ENTITY_TYPE,
                    new Identifier("sofias_ordainment", "pure_sculk"),
                    FabricEntityTypeBuilder.<CustomPotionEntity>create(SpawnGroup.MISC, CustomPotionEntity::new)
                            .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                            .trackRangeBlocks(4)
                            .trackedUpdateRate(10)
                            .build()
            );

    public static final Item CUSTOM_POTION =
            Registry.register(
                    Registries.ITEM,
                    new Identifier("sofias_ordainment", "pure_sculk_potion"),
                    new CustomPotionItem(new Item.Settings().maxCount(1))
            );

    public static void init() {
        Sculkified.init();

        Registry.register(ApoliRegistries.POWER_FACTORY, SOUL_CONVERSION.getSerializerId(), SOUL_CONVERSION);
        Registry.register(ApoliRegistries.POWER_FACTORY, SOUL_STAIN.getSerializerId(), SOUL_STAIN);
        Registry.register(ApoliRegistries.POWER_FACTORY, SUCCULENCE.getSerializerId(), SUCCULENCE);
        Registry.register(ApoliRegistries.POWER_FACTORY, SCULKED_SIGHT.getSerializerId(), SCULKED_SIGHT);
        Registry.register(ApoliRegistries.POWER_FACTORY, DISABLE_CRIT.getSerializerId(), DISABLE_CRIT);
        Registry.register(ApoliRegistries.POWER_FACTORY, SCREECH.getSerializerId(), SCREECH);
        Registry.register(ApoliRegistries.POWER_FACTORY, GROUNDING.getSerializerId(), GROUNDING);

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            StateSaverAndLoader state = StateSaverAndLoader.getServerState(newPlayer.getServer());

            if (state.permSculked.contains(newPlayer.getUuid())) {
                StatusEffectInstance statusEffectInstance = new StatusEffectInstance(SCULKIFIED, -1, 0);
                newPlayer.addStatusEffect(statusEffectInstance);
                SculkifiedSourceTrackerHelper.refreshTargetStackCount(newPlayer);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                // Only check every 20 ticks (1 second) to save performance
                if (server.getTicks() % 20 == 0) {
                    if (PowerHolderComponent.hasPower(player, Succulence.class)) {
                        PowerHolderComponent.getPowers(player, Succulence.class).forEach(Succulence::tick);
                    }
                }
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();

            StateSaverAndLoader state = StateSaverAndLoader.getServerState(server);

            if (state.permSculked.contains(player.getUuid())) {
                player.addStatusEffect(new StatusEffectInstance(RegistryHelper.SCULKIFIED, -1, 2));
                SculkifiedSourceTrackerHelper.refreshTargetStackCount(player);
            }
        });
    }
}
