package com.watsonllc.chestlock.logic;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

public class HopperDecisionSimulator {

        public static DecisionResult evaluate(UntaggedHopperPolicy policy, String hopperOwner, boolean sourceProtected,
                        boolean destinationProtected, boolean conflictingOwners, Set<String> sourceAllowedPlayers,
                        Set<String> destinationAllowedPlayers, Supplier<String> tagSupplier) {

                if (conflictingOwners)
                        return new DecisionResult(false, hopperOwner, false);

                boolean hasProtectedLock = sourceProtected || destinationProtected;

                if (hopperOwner == null && hasProtectedLock) {
                        switch (policy) {
                        case ALLOW:
                                return new DecisionResult(true, null, false);
                        case TAG_ON_USE:
                                if (tagSupplier != null) {
                                        hopperOwner = tagSupplier.get();
                                        return new DecisionResult(true, hopperOwner, true);
                                }
                                return new DecisionResult(true, null, false);
                        case DENY:
                        default:
                                return new DecisionResult(false, null, false);
                        }
                }

                boolean allowed = true;
                String normalized = hopperOwner == null ? null : hopperOwner.toLowerCase();

                if (sourceProtected && normalized != null
                                && !safeSet(sourceAllowedPlayers).contains(normalized))
                        allowed = false;
                if (destinationProtected && normalized != null
                                && !safeSet(destinationAllowedPlayers).contains(normalized))
                        allowed = false;

                return new DecisionResult(allowed, hopperOwner, false);
        }

        private static Set<String> safeSet(Set<String> players) {
            return players == null ? Collections.emptySet() : players;
        }

        public static class DecisionResult {
                public final boolean allowed;
                public final String resolvedOwner;
                public final boolean tagged;

                public DecisionResult(boolean allowed, String resolvedOwner, boolean tagged) {
                        this.allowed = allowed;
                        this.resolvedOwner = resolvedOwner;
                        this.tagged = tagged;
                }
        }
}
