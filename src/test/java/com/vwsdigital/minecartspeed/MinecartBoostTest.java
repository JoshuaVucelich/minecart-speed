package com.vwsdigital.minecartspeed;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MinecartBoostTest {
	private static final double BOOST_PER_TICK = 120.0 / 20.0;

	@Test
	void rampStartsAtVanillaSpeed() {
		assertEquals(
				MinecartBoost.VANILLA_MAX_SPEED,
				MinecartBoost.rampedTarget(BOOST_PER_TICK, 0.0, 3.0),
				1.0e-9);
	}

	@Test
	void rampReachesBoostAfterTheConfiguredDistance() {
		assertEquals(BOOST_PER_TICK, MinecartBoost.rampedTarget(BOOST_PER_TICK, 3.0, 3.0), 1.0e-9);
		assertEquals(BOOST_PER_TICK, MinecartBoost.rampedTarget(BOOST_PER_TICK, 9.0, 3.0), 1.0e-9);
	}

	@Test
	void zeroRampDistanceSkipsToFullBoost() {
		assertEquals(BOOST_PER_TICK, MinecartBoost.rampedTarget(BOOST_PER_TICK, 0.0, 0.0), 1.0e-9);
	}

	@Test
	void rampIsHalfwayAtHalfTheDistance() {
		double expected = MinecartBoost.VANILLA_MAX_SPEED
				+ (BOOST_PER_TICK - MinecartBoost.VANILLA_MAX_SPEED) * 0.5;
		assertEquals(expected, MinecartBoost.rampedTarget(BOOST_PER_TICK, 1.5, 3.0), 1.0e-9);
	}
}
