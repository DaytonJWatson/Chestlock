package com.watsonllc.chestlock.logic;

import java.util.Objects;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;

public class BlockKey implements Comparable<BlockKey> {
        private final UUID worldId;
        private final int x;
        private final int y;
        private final int z;

        public BlockKey(UUID worldId, int x, int y, int z) {
                this.worldId = worldId;
                this.x = x;
                this.y = y;
                this.z = z;
        }

        public static BlockKey fromLocation(Location location) {
                if (location == null)
                        return null;

                World world = location.getWorld();
                if (world == null)
                        return null;

                return new BlockKey(world.getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        }

        public UUID getWorldId() {
                return worldId;
        }

        public int getX() {
                return x;
        }

        public int getY() {
                return y;
        }

        public int getZ() {
                return z;
        }

        @Override
        public boolean equals(Object o) {
                if (this == o)
                        return true;
                if (!(o instanceof BlockKey))
                        return false;
                BlockKey blockKey = (BlockKey) o;
                return x == blockKey.x && y == blockKey.y && z == blockKey.z && Objects.equals(worldId, blockKey.worldId);
        }

        @Override
        public int hashCode() {
                return Objects.hash(worldId, x, y, z);
        }

        @Override
        public int compareTo(BlockKey other) {
                if (other == null)
                        return 1;

                int worldCompare = this.worldId.compareTo(other.worldId);
                if (worldCompare != 0)
                        return worldCompare;

                if (this.x != other.x)
                        return Integer.compare(this.x, other.x);
                if (this.y != other.y)
                        return Integer.compare(this.y, other.y);
                return Integer.compare(this.z, other.z);
        }

        @Override
        public String toString() {
                return worldId + ":" + x + "," + y + "," + z;
        }
}
