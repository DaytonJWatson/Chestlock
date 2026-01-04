package com.watsonllc.chestlock.logic;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class LockEntry {
        private final String lockId;
        private final BlockKey blockKey;
        private final String owner;
        private final Set<String> allowedPlayers;
        private final boolean isPublic;
        private final boolean allowHoppers;
        private final String type;

        public LockEntry(String lockId, BlockKey blockKey, String owner, Set<String> allowedPlayers, boolean isPublic, boolean allowHoppers, String type) {
                this.lockId = lockId;
                this.blockKey = blockKey;
                this.owner = owner;
                this.allowedPlayers = allowedPlayers == null ? Collections.emptySet() : new LinkedHashSet<>(allowedPlayers);
                this.isPublic = isPublic;
                this.allowHoppers = allowHoppers;
                this.type = type;
        }

        public String getLockId() {
                return lockId;
        }

        public BlockKey getBlockKey() {
                return blockKey;
        }

        public String getOwner() {
                return owner;
        }

        public Set<String> getAllowedPlayers() {
                return new LinkedHashSet<>(allowedPlayers);
        }

        public boolean isPublic() {
                return isPublic;
        }

        public boolean isAllowHoppers() {
                return allowHoppers;
        }

        public String getType() {
                return type;
        }

        public LockEntry withAllowedPlayers(Set<String> updatedAllowed) {
                String nextOwner = updatedAllowed == null || updatedAllowed.isEmpty() ? null : updatedAllowed.iterator().next();
                return new LockEntry(lockId, blockKey, nextOwner, updatedAllowed, isPublic, allowHoppers, type);
        }

        public LockEntry withPublic(boolean newPublic) {
                return new LockEntry(lockId, blockKey, owner, allowedPlayers, newPublic, allowHoppers, type);
        }

        public LockEntry withLocation(BlockKey newKey) {
                return new LockEntry(lockId, newKey, owner, allowedPlayers, isPublic, allowHoppers, type);
        }
}
