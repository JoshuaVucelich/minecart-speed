package com.vwsdigital.minecartspeed;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MinecartSpeed.MOD_ID)
public class MinecartSpeed {
	public static final String MOD_ID = "minecart_speed";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final Permission OP_PERMISSION =
			new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS);

	private static volatile MinecartSpeedConfig CONFIG;

	public static MinecartSpeedConfig config() {
		return CONFIG;
	}

	public MinecartSpeed(IEventBus modEventBus) {
		CONFIG = MinecartSpeedConfig.loadOrCreate();
		LOGGER.info("Minecart Speed loaded — boost {} blocks/sec, triggers: {}",
				CONFIG.boostSpeed, CONFIG.triggerBlocks);

		NeoForge.EVENT_BUS.addListener(MinecartSpeed::registerCommands);
	}

	private static void registerCommands(RegisterCommandsEvent event) {
		event.getDispatcher().register(Commands.literal("minecartspeed")
				.requires(source -> source.permissions().hasPermission(OP_PERMISSION))
				.then(Commands.literal("reload").executes(ctx -> {
					CONFIG = MinecartSpeedConfig.loadOrCreate();
					ctx.getSource().sendSuccess(
							() -> Component.literal("Minecart Speed config reloaded ("
									+ CONFIG.boostSpeed + " blocks/sec)"),
							true);
					return 1;
				})));
	}
}
