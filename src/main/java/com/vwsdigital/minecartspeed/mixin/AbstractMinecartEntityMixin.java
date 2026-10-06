package com.vwsdigital.minecartspeed.mixin;

import com.vwsdigital.minecartspeed.MinecartBoost;
import com.vwsdigital.minecartspeed.MinecartSpeed;
import com.vwsdigital.minecartspeed.MinecartSpeedConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartEntityMixin {

    @Unique
    private static final int MINECART_SPEED$BOOST_GRACE_TICKS = 20;

    @Shadow protected abstract void moveAlongTrack(ServerLevel level);
    @Shadow public abstract boolean isOnRails();

    @Unique
    private double minecartSpeed$remainingDistance = 0.0;

    @Unique
    private double minecartSpeed$boostDistance = 0.0;

    @Unique
    private int minecartSpeed$boostedTicks = 0;

    @Unique
    private BlockPos minecartSpeed$lastRailPos = null;

    @Unique
    private Vec3 minecartSpeed$lastDirection = Vec3.ZERO;

    @Inject(method = "tick", at = @At("HEAD"))
    private void minecartSpeed$preTick(CallbackInfo ci) {
        minecartSpeed$remainingDistance = 0.0;

        AbstractMinecart self = (AbstractMinecart) (Object) this;
        MinecartSpeedConfig config = MinecartSpeed.config();
        if (config == null || !config.enabled) return;
        Level level = self.level();
        if (level.isClientSide()) return;

        MinecartBoost.Plan plan = MinecartBoost.plan(self, config);
        if (plan == null) {
            // Not on a mod-controlled rail — stay out of vanilla's way completely.
            minecartSpeed$boostDistance = 0.0;
            return;
        }

        Vec3 motion = self.getDeltaMovement();
        double horizontal = Math.hypot(motion.x, motion.z);
        if (horizontal < 1.0e-6) return;

        // Only track rail position for carts we're actively managing,
        // so vanilla carts that intentionally leave rails don't get snapped back.
        BlockPos railPos = minecartSpeed$findRailPos(level, self);
        if (railPos != null) {
            minecartSpeed$lastRailPos = railPos;
            minecartSpeed$lastDirection = motion;
        }

        double targetPerTick;
        if (plan.boost()) {
            double rampBlocks = Math.max(0.0, config.accelerationBlocks);
            double ramped = MinecartBoost.rampedTarget(plan.maxSpeedBlocksPerTick(), minecartSpeed$boostDistance, rampBlocks);
            targetPerTick = Math.max(ramped, horizontal);
            minecartSpeed$boostDistance += targetPerTick;
        } else {
            targetPerTick = plan.maxSpeedBlocksPerTick();
            minecartSpeed$boostDistance = 0.0;
        }
        minecartSpeed$boostedTicks = MINECART_SPEED$BOOST_GRACE_TICKS;

        double firstStep = Math.min(targetPerTick, MinecartBoost.VANILLA_MAX_SPEED);
        double scale = firstStep / horizontal;
        self.setDeltaMovement(motion.x * scale, motion.y, motion.z * scale);
        self.syncVelocity = true;

        if (plan.boost() && config.ejectPassengersOnBoost && self.isVehicle()) {
            self.ejectPassengers();
        }

        minecartSpeed$remainingDistance = Math.max(0.0, targetPerTick - firstStep);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void minecartSpeed$postTick(CallbackInfo ci) {
        double remaining = minecartSpeed$remainingDistance;
        minecartSpeed$remainingDistance = 0.0;

        AbstractMinecart self = (AbstractMinecart) (Object) this;
        if (!(self.level() instanceof ServerLevel server)) return;
        MinecartSpeedConfig config = MinecartSpeed.config();
        if (config == null || !config.enabled) return;

        int safety = 0;
        while (remaining > 0 && self.isOnRails() && safety++ < 200) {
            if (MinecartBoost.plan(self, config) == null) break;

            Vec3 m = self.getDeltaMovement();
            double h = Math.hypot(m.x, m.z);
            if (h < 1.0e-6) break;

            double step = Math.min(remaining, MinecartBoost.VANILLA_MAX_SPEED);
            double scale = step / h;
            self.setDeltaMovement(m.x * scale, m.y, m.z * scale);
            moveAlongTrack(server);
            remaining -= step;
        }

        // Snap-to-rails only applies to carts the mod just boosted. For every
        // other cart we behave exactly like vanilla so redstone contraptions
        // (sorters, drop tracks, hopper-cart traps, etc.) keep working.
        if (minecartSpeed$boostedTicks <= 0) {
            minecartSpeed$lastRailPos = null;
            return;
        }
        minecartSpeed$boostedTicks--;

        if (!config.keepOnRails) return;
        if (self.isOnRails()) {
            BlockPos railPos = minecartSpeed$findRailPos(server, self);
            if (railPos != null) minecartSpeed$lastRailPos = railPos;
            return;
        }
        if (minecartSpeed$lastRailPos == null) return;

        BlockPos snap = minecartSpeed$findNearbyRail(server, minecartSpeed$lastRailPos, minecartSpeed$lastDirection);
        if (snap != null) {
            self.setPos(snap.getX() + 0.5, snap.getY() + 0.0625, snap.getZ() + 0.5);
            Vec3 m = self.getDeltaMovement();
            self.setDeltaMovement(m.x, 0.0, m.z);
            self.setOnGround(true);
            self.syncVelocity = true;
            minecartSpeed$lastRailPos = snap;
        } else {
            minecartSpeed$lastRailPos = null;
        }
    }

    @Inject(method = "getMaxSpeed(Lnet/minecraft/server/level/ServerLevel;)D", at = @At("RETURN"), cancellable = true, require = 0)
    private void minecartSpeed$maxSpeed(ServerLevel level, CallbackInfoReturnable<Double> cir) {
        MinecartSpeedConfig config = MinecartSpeed.config();
        if (config == null || !config.enabled) return;

        // Don't raise the cap globally — that turned every vanilla powered rail
        // on the server into a super-booster and threw carts off curves in
        // redstone farms. Only raise it for carts the mod is currently or was
        // just managing.
        if (minecartSpeed$boostedTicks <= 0) {
            AbstractMinecart self = (AbstractMinecart) (Object) this;
            if (MinecartBoost.plan(self, config) == null) return;
        }

        double perTick = Math.max(0.0, config.boostSpeed) / 20.0;
        if (perTick > cir.getReturnValueD()) {
            cir.setReturnValue(perTick);
        }
    }

    @Unique
    private static BlockPos minecartSpeed$findRailPos(Level level, AbstractMinecart cart) {
        BlockPos at = BlockPos.containing(cart.getX(), cart.getY(), cart.getZ());
        if (level.getBlockState(at).getBlock() instanceof BaseRailBlock) return at;
        BlockPos below = at.below();
        if (level.getBlockState(below).getBlock() instanceof BaseRailBlock) return below;
        return null;
    }

    @Unique
    private static BlockPos minecartSpeed$findNearbyRail(Level level, BlockPos lastRailPos, Vec3 direction) {
        if (level.getBlockState(lastRailPos).getBlock() instanceof BaseRailBlock) {
            return lastRailPos;
        }
        int dx = direction.x > 0.05 ? 1 : direction.x < -0.05 ? -1 : 0;
        int dz = direction.z > 0.05 ? 1 : direction.z < -0.05 ? -1 : 0;

        BlockPos[] candidates;
        if (dx != 0 || dz != 0) {
            candidates = new BlockPos[] {
                lastRailPos.offset(dx, 0, dz),
                lastRailPos.offset(dx, 1, dz),
                lastRailPos.offset(dx, -1, dz),
                lastRailPos.above(),
                lastRailPos.below(),
            };
        } else {
            candidates = new BlockPos[] {
                lastRailPos.above(),
                lastRailPos.below(),
            };
        }
        for (BlockPos pos : candidates) {
            if (level.getBlockState(pos).getBlock() instanceof BaseRailBlock) {
                return pos;
            }
        }
        return null;
    }
}
