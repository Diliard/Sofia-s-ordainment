package de.sofia.sofias_ordainment;

import de.sofia.sofias_ordainment.effects.Sculkified;
import de.sofia.sofias_ordainment.entity.CustomPotionEntity;
import de.sofia.sofias_ordainment.origins.powers.*;
import de.sofia.sofias_ordainment.origins.utility.SculkifiedSourceTrackerHelper;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.CooldownPower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
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
import net.minecraft.server.network.ServerPlayerEntity;
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

    public static final HudRender INFECTIOUS_LUNGE_HUD = new HudRender(
            true,
            0,
            new Identifier(MOD_ID, "textures/gui/resource_bar.png"),
            null,
            false
    );

    public static final HudRender PARTHENOGENESIS_HUD = new HudRender(
            true,
            1,
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

    public static final PowerFactory<CooldownPower> INFECTIOUS_LUNGE =
            new PowerFactory<CooldownPower>(
                    new Identifier(MOD_ID, "infectious_lunge"),
                    new SerializableData()
                            .add("cooldown", SerializableDataTypes.INT, 300)
                            .add("hud_render", ApoliDataTypes.HUD_RENDER, INFECTIOUS_LUNGE_HUD)
                            .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
                    data -> (type, player) -> {
                        InfectiousLungePower power = new InfectiousLungePower(type, player, data.getInt("cooldown"), data.get("hud_render"));
                        power.setKey(data.get("key"));
                        return power;
                    }
            ).allowCondition();

    public static final PowerFactory<CooldownPower> PARTHENOGENESIS =
            new PowerFactory<CooldownPower>(
                    new Identifier(MOD_ID, "parthenogenesis"),
                    new SerializableData()
                            .add("cooldown", SerializableDataTypes.INT, 1200)
                            .add("hud_render", ApoliDataTypes.HUD_RENDER, PARTHENOGENESIS_HUD)
                            .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
                    data -> (type, player) -> {
                        ParthenogenesisPower power = new ParthenogenesisPower(type, player, data.getInt("cooldown"), data.get("hud_render"));
                        power.setKey(data.get("key"));
                        return power;
                    }
            ).allowCondition();

    public static final PowerFactory<Power> BOTTOM_FEEDER =
            new PowerFactory<>(
                    new Identifier(MOD_ID, "bottom_feeder"),
                    new SerializableData(),
                    data -> (type, player) -> new BottomFeederPower(type, player)
            ).allowCondition();

    public static final PowerFactory<Power> LOW_HEALTH_SWIFTNESS =
            new PowerFactory<>(
                    new Identifier(MOD_ID, "low_health_swiftness"),
                    new SerializableData(),
                    data -> (type, player) -> new LowHealthSwiftnessPower(type, player)
            ).allowCondition();

    public static final PowerFactory<Power> CANNOT_SWIM =
            new PowerFactory<>(
                    new Identifier(MOD_ID, "cannot_swim"),
                    new SerializableData(),
                    data -> (type, player) -> new CannotSwimPower(type, player)
            ).allowCondition();

    public static final PowerFactory<Power> FAST_DROWNING =
            new PowerFactory<>(
                    new Identifier(MOD_ID, "fast_drowning"),
                    new SerializableData(),
                    data -> (type, player) -> new FastDrowningPower(type, player)
            ).allowCondition();

    public static final PowerFactory<Power> FIRE_SENSITIVITY =
            new PowerFactory<>(
                    new Identifier(MOD_ID, "fire_sensitivity"),
                    new SerializableData(),
                    data -> (type, player) -> new FireSensitivityPower(type, player)
            ).allowCondition();

    public static final PowerFactory<Power> SOAP_WEAKNESS =
            new PowerFactory<>(
                    new Identifier(MOD_ID, "soap_weakness"),
                    new SerializableData(),
                    data -> (type, player) -> new SoapWeaknessPower(type, player)
            ).allowCondition();

    public static final PowerFactory<Power> PESTILENCE =
            new PowerFactory<>(
                    new Identifier(MOD_ID, "pestilence"),
                    new SerializableData(),
                    data -> (type, player) -> new Pestilence(type, player, 0, HudRender.DONT_RENDER, damage -> true, pair -> {}, pair -> true)
            ).allowCondition();

    public static final PowerFactory<Power> BULKY_EXOSKELETON =
            new PowerFactory<>(
                    new Identifier(MOD_ID, "bulky_exoskeleton"),
                    new SerializableData(),
                    data -> (type, player) -> new BulkyExoskeleton(type, player, damage -> true, null)
            ).allowCondition();

    public static final PowerFactory<Power> SHED_EXOSKELETON =
            new PowerFactory<>(
                    new Identifier(MOD_ID, "shed_exoskeleton"),
                    new SerializableData(),
                    data -> (type, player) -> new ShedExoskeleton(type, player, false)
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
        Registry.register(ApoliRegistries.POWER_FACTORY, INFECTIOUS_LUNGE.getSerializerId(), INFECTIOUS_LUNGE);
        Registry.register(ApoliRegistries.POWER_FACTORY, PARTHENOGENESIS.getSerializerId(), PARTHENOGENESIS);
        Registry.register(ApoliRegistries.POWER_FACTORY, BOTTOM_FEEDER.getSerializerId(), BOTTOM_FEEDER);
        Registry.register(ApoliRegistries.POWER_FACTORY, LOW_HEALTH_SWIFTNESS.getSerializerId(), LOW_HEALTH_SWIFTNESS);
        Registry.register(ApoliRegistries.POWER_FACTORY, CANNOT_SWIM.getSerializerId(), CANNOT_SWIM);
        Registry.register(ApoliRegistries.POWER_FACTORY, FAST_DROWNING.getSerializerId(), FAST_DROWNING);
        Registry.register(ApoliRegistries.POWER_FACTORY, FIRE_SENSITIVITY.getSerializerId(), FIRE_SENSITIVITY);
        Registry.register(ApoliRegistries.POWER_FACTORY, SOAP_WEAKNESS.getSerializerId(), SOAP_WEAKNESS);
        Registry.register(ApoliRegistries.POWER_FACTORY, PESTILENCE.getSerializerId(), PESTILENCE);
        Registry.register(ApoliRegistries.POWER_FACTORY, BULKY_EXOSKELETON.getSerializerId(), BULKY_EXOSKELETON);
        Registry.register(ApoliRegistries.POWER_FACTORY, SHED_EXOSKELETON.getSerializerId(), SHED_EXOSKELETON);

        ParthenogenesisPower.registerEvents();

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
