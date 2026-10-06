package com.vwsdigital.minecartspeed;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinecartSpeed implements ModInitializer {
	public static final String MOD_ID = "minecart-speed";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final Permission OP_PERMISSION =
			new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS);

	private static volatile MinecartSpeedConfig CONFIG;

	public static MinecartSpeedConfig config() {
		return CONFIG;
	}

	@Override
	public void onInitialize() {
		CONFIG = MinecartSpeedConfig.loadOrCreate();
		LOGGER.info("Minecart Speed loaded — boost {} blocks/sec, triggers: {}",
				CONFIG.boostSpeed, CONFIG.triggerBlocks);

		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) -> {
			dispatcher.register(Commands.literal("minecartspeed")
					.requires(source -> source.permissions().hasPermission(OP_PERMISSION))
					.then(Commands.literal("reload").executes(ctx -> {
						CONFIG = MinecartSpeedConfig.loadOrCreate();
						ctx.getSource().sendSuccess(
								() -> Component.literal("Minecart Speed config reloaded ("
										+ CONFIG.boostSpeed + " blocks/sec)"),
								true);
						return 1;
					})));
		});
	}
}
