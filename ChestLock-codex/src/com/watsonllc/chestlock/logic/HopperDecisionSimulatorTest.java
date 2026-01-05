package com.watsonllc.chestlock.logic;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class HopperDecisionSimulatorTest {

        public static void main(String[] args) {
                runAll();
        }

        public static void runAll() {
                testUntaggedAllowedWithAllowPolicy();
                testUntaggedDeniedWithDenyPolicy();
                testTagOnUseAppliesOwnerAndAllows();
                testTaggedHopperHonorsPermissions();
                testUnlockedTransfersBypassProtection();
        }

        private static void testUntaggedAllowedWithAllowPolicy() {
                HopperDecisionSimulator.DecisionResult result = HopperDecisionSimulator.evaluate(
                                UntaggedHopperPolicy.ALLOW, null, false, true, false, allowed("alice"),
                                allowed("alice"), null);

                require(result.allowed, "Untagged hopper should be allowed when policy is ALLOW");
                require(!result.tagged, "ALLOW policy should not tag hoppers");
        }

        private static void testUntaggedDeniedWithDenyPolicy() {
                HopperDecisionSimulator.DecisionResult result = HopperDecisionSimulator.evaluate(
                                UntaggedHopperPolicy.DENY, null, false, true, false, allowed("alice"),
                                allowed("alice"), null);

                require(!result.allowed, "Untagged hopper should be denied when policy is DENY");
        }

        private static void testTagOnUseAppliesOwnerAndAllows() {
                AtomicBoolean tagged = new AtomicBoolean(false);
                HopperDecisionSimulator.DecisionResult result = HopperDecisionSimulator.evaluate(
                                UntaggedHopperPolicy.TAG_ON_USE, null, false, true, false, allowed("alice"),
                                allowed("alice"), () -> {
                                        tagged.set(true);
                                        return "alice";
                                });

                require(result.allowed, "TAG_ON_USE should allow hopper after tagging");
                require(tagged.get(), "TAG_ON_USE should invoke tag supplier");
                require("alice".equals(result.resolvedOwner), "Tagged hopper should resolve to supplied owner");
        }

        private static void testTaggedHopperHonorsPermissions() {
                HopperDecisionSimulator.DecisionResult allowedResult = HopperDecisionSimulator.evaluate(
                                UntaggedHopperPolicy.DENY, "alice", false, true, false, allowed("alice"),
                                allowed("alice"), null);
                HopperDecisionSimulator.DecisionResult deniedResult = HopperDecisionSimulator.evaluate(
                                UntaggedHopperPolicy.DENY, "bob", false, true, false, allowed("alice"),
                                allowed("alice"), null);

                require(allowedResult.allowed, "Tagged hopper owned by allowed player should pass");
                require(!deniedResult.allowed, "Tagged hopper owned by disallowed player should fail");
        }

        private static void testUnlockedTransfersBypassProtection() {
                HopperDecisionSimulator.DecisionResult result = HopperDecisionSimulator.evaluate(
                                UntaggedHopperPolicy.DENY, null, false, false, false, allowed(),
                                allowed(), null);

                require(result.allowed, "Transfers without protected locks should bypass protection");
        }

        private static Set<String> allowed(String... players) {
                Set<String> allowed = new LinkedHashSet<>();
                for (String player : players) {
                        allowed.add(player);
                }
                return allowed;
        }

        private static void require(boolean condition, String message) {
                if (!condition)
                        throw new IllegalStateException(message);
        }
}
