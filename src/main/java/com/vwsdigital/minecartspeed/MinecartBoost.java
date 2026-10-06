package com.vwsdigital.minecartspeed;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class MinecartBoost {

    public static final double VANILLA_MAX_SPEED = 0.4;

    private MinecartBoost() {}

    public record Plan(double maxSpeedBlocksPerTick, boolean boost) {}

    public static Plan plan(AbstractMinecart cart, MinecartSpeedConfig config) {
        if (config == null || !config.enabled) return null;

        Level level = cart.level();
        if (level.isClientSide()) return null;

        BlockPos cartPos = BlockPos.containing(cart.getX(), cart.getY(), cart.getZ());
        BlockState rail = level.getBlockState(cartPos);
        BlockPos railPos;
        if (rail.getBlock() instanceof BaseRailBlock) {
            railPos = cartPos;
        } else {
            BlockPos below = cartPos.below();
            BlockState belowState = level.getBlockState(below);
            if (!(belowState.getBlock() instanceof BaseRailBlock)) return null;
            rail = belowState;
            railPos = below;
        }

        BlockState trigger = level.getBlockState(railPos.below());
        if (!config.isTriggerBlock(trigger.getBlock())) return null;

        boolean isPowered = rail.getBlock() instanceof PoweredRailBlock;
        boolean railIsLive = isPowered && rail.getValue(PoweredRailBlock.POWERED);
        boolean shouldBoost = isPowered && (railIsLive || !config.requirePowered);

        double maxBoostPerTick = Math.max(0.0, config.boostSpeed) / 20.0;
        double horizontal = Math.hypot(cart.getDeltaMovement().x, cart.getDeltaMovement().z);

        if (shouldBoost) {
            return new Plan(maxBoostPerTick, true);
        }
        // Non-boost triggered rail: hold cart on track at whatever speed it has.
        if (horizontal < VANILLA_MAX_SPEED) return null;
        return new Plan(horizontal, false);
    }

    public static double rampedTarget(double maxBoostPerTick, double accumulatedDistance, double rampDistanceBlocks) {
        if (rampDistanceBlocks <= 0.0) return maxBoostPerTick;
        double progress = Math.min(1.0, Math.max(0.0, accumulatedDistance) / rampDistanceBlocks);
        return VANILLA_MAX_SPEED + (maxBoostPerTick - VANILLA_MAX_SPEED) * progress;
    }
}
