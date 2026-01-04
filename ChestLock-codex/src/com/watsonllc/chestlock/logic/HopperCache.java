package com.watsonllc.chestlock.logic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;

import com.watsonllc.chestlock.Main;

public class HopperCache {
        private static final Map<HopperDecisionKey, HopperDecision> CACHE = new ConcurrentHashMap<>();
        private static volatile long currentTick = 0L;
        private static int ttlTicks = 20;
        private static boolean ticking = false;

        public static void configureTtl(int configuredTtl) {
                ttlTicks = Math.max(0, Math.min(200, configuredTtl));
        }

        public static void startTickCounter() {
                if (ticking)
                        return;

                ticking = true;
                Bukkit.getScheduler().runTaskTimer(Main.instance, () -> currentTick++, 1L, 1L);
        }

        public static Boolean get(HopperDecisionKey key) {
                if (ttlTicks <= 0)
                        return null;

                HopperDecision decision = CACHE.get(key);
                if (decision == null)
                        return null;

                if (decision.expiryTick <= currentTick) {
                        CACHE.remove(key);
                        return null;
                }

                return decision.allowed;
        }

        public static void put(HopperDecisionKey key, boolean allowed) {
                if (ttlTicks <= 0)
                        return;

                CACHE.put(key, new HopperDecision(allowed, currentTick + ttlTicks));
        }

        public static void invalidate() {
                CACHE.clear();
        }

        public static long getCurrentTick() {
                return currentTick;
        }

        private static class HopperDecision {
                private final boolean allowed;
                private final long expiryTick;

                private HopperDecision(boolean allowed, long expiryTick) {
                        this.allowed = allowed;
                        this.expiryTick = expiryTick;
                }
        }
}
