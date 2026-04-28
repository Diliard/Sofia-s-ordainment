package de.sofia.sofias_ordainment;

import com.mojang.brigadier.arguments.BoolArgumentType;
import de.sofia.sofias_ordainment.origins.powers.Sculked_Sight;
import de.sofia.sofias_ordainment.origins.utility.SculkifiedSourceTrackerHelper;
import io.github.apace100.apoli.component.PowerHolderComponent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sofias_ordainment implements ModInitializer {
    public static final String MOD_ID = "sofias_ordainment";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        RegistryHelper.init();

        ResourceManagerHelper.registerBuiltinResourcePack(
                new Identifier("sofias_ordainment", "builtin"),
                FabricLoader.getInstance().getModContainer("sofias_ordainment").orElseThrow(),
                Text.literal("Sofia's Ordainment Origins"),
                ResourcePackActivationType.ALWAYS_ENABLED
        );

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("removePermSculk") // The base command name
                    .then(CommandManager.argument("target", EntityArgumentType.player())
                            .requires(source -> source.hasPermissionLevel(2))// The argument
                            .executes(context -> {
                                ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "target");
                                StateSaverAndLoader state = StateSaverAndLoader.getServerState(context.getSource().getServer());

                                if (state.permSculked.contains(targetPlayer.getUuid())) {
                                    state.permSculked.remove(targetPlayer.getUuid());
                                    targetPlayer.removeStatusEffect(RegistryHelper.SCULKIFIED);
                                    SculkifiedSourceTrackerHelper.removeTarget(targetPlayer);
                                    state.markDirty();
                                    // Example: Send feedback to the command sender
                                    context.getSource().sendFeedback(() -> Text.literal("Removed " + targetPlayer.getName().getString() + " from Permsculked players."), true);

                                    return 1; // Return 1 for success
                                }
                                else
                                {
                                    context.getSource().sendFeedback(() -> Text.literal("Player isn't permsculked."), false);
                                    return 0;
                                }
                            })
                    )
            );
            dispatcher.register(CommandManager.literal("sculkedSightSensorRelay")
                    .executes(context -> toggleSculkedSightSensorRelay(context.getSource().getPlayer()))
                    .then(CommandManager.literal("on")
                            .executes(context -> setSculkedSightSensorRelay(context.getSource().getPlayer(), true))
                    )
                    .then(CommandManager.literal("off")
                            .executes(context -> setSculkedSightSensorRelay(context.getSource().getPlayer(), false))
                    )
                    .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                            .executes(context -> setSculkedSightSensorRelay(
                                    context.getSource().getPlayer(),
                                    BoolArgumentType.getBool(context, "enabled")
                            ))
                    )
            );
            dispatcher.register(CommandManager.literal("sculkedSightShriekerRelay")
                    .executes(context -> toggleSculkedSightSensorRelay(context.getSource().getPlayer()))
                    .then(CommandManager.literal("on")
                            .executes(context -> setSculkedSightSensorRelay(context.getSource().getPlayer(), true))
                    )
                    .then(CommandManager.literal("off")
                            .executes(context -> setSculkedSightSensorRelay(context.getSource().getPlayer(), false))
                    )
                    .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                            .executes(context -> setSculkedSightSensorRelay(
                                    context.getSource().getPlayer(),
                                    BoolArgumentType.getBool(context, "enabled")
                            ))
                    )
            );
        });
    }

    private int toggleSculkedSightSensorRelay(ServerPlayerEntity player) {
        StateSaverAndLoader state = StateSaverAndLoader.getServerState(player.getServer());
        boolean enabled = state.isSculkedSightSensorRelayDisabled(player.getUuid());

        return setSculkedSightSensorRelay(player, enabled);
    }

    private int setSculkedSightSensorRelay(ServerPlayerEntity player, boolean enabled) {
        if (!PowerHolderComponent.hasPower(player, Sculked_Sight.class)) {
            player.sendMessage(Text.literal("You may only use this while you have Sculked Sight."));
            return 0;
        }

        StateSaverAndLoader state = StateSaverAndLoader.getServerState(player.getServer());
        state.setSculkedSightSensorRelayDisabled(player.getUuid(), !enabled);

        String status = enabled ? "enabled" : "disabled";
        Text message = Text.literal("Sculked Sight sensor voice relay " + status + ".");

        player.sendMessage(message, false);
        return 1;
    }
}
