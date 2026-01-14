package com.watsonllc.chestlock;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.InventoryHolder;

import com.watsonllc.chestlock.config.Config;

public class Utils {
	public static String color(String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}
	
	public static boolean lockableBlock(Block block) {
		switch (block.getType()) {
		case ANVIL:
			return Config.getBoolean("lockables.ANVIL");
		case BARREL:
			return Config.getBoolean("lockables.BARREL");
		case BLAST_FURNACE:
			return Config.getBoolean("lockables.BLAST_FURNACE");
		case CHEST:
			return Config.getBoolean("lockables.CHEST");
		case COPPER_CHEST:
			return Config.getBoolean("lockables.CHEST");
		case EXPOSED_COPPER_CHEST:
			return Config.getBoolean("lockables.CHEST");
		case WEATHERED_COPPER_CHEST:
			return Config.getBoolean("lockables.CHEST");
		case OXIDIZED_COPPER_CHEST:
			return Config.getBoolean("lockables.CHEST");
		case WAXED_COPPER_CHEST:
			return Config.getBoolean("lockables.CHEST");
		case WAXED_EXPOSED_COPPER_CHEST:
			return Config.getBoolean("lockables.CHEST");
		case WAXED_WEATHERED_COPPER_CHEST:
			return Config.getBoolean("lockables.CHEST");
		case WAXED_OXIDIZED_COPPER_CHEST:
			return Config.getBoolean("lockables.CHEST");
		case DISPENSER:
			return Config.getBoolean("lockables.DISPENSER");
		case DROPPER:
			return Config.getBoolean("lockables.DROPPER");
		case ENCHANTING_TABLE:
		    return Config.getBoolean("lockables.ENCHANTING_TABLE");
		case ENDER_CHEST:
			return Config.getBoolean("lockables.ENDER_CHEST");
		case FURNACE:
			return Config.getBoolean("lockables.FURNACE");
		case HOPPER:
			return Config.getBoolean("lockables.HOPPER");
		case JUKEBOX: 
			return Config.getBoolean("lockables.JUKEBOX");
		case SHULKER_BOX:
			return Config.getBoolean("lockables.SHULKER_BOX");
		case WHITE_SHULKER_BOX:
			return Config.getBoolean("lockables.SHULKER_BOX");
		case LIGHT_GRAY_SHULKER_BOX:
			return Config.getBoolean("lockables.SHULKER_BOX");
		case GRAY_SHULKER_BOX:
			return Config.getBoolean("lockables.SHULKER_BOX");
		case BLACK_SHULKER_BOX:
			return Config.getBoolean("lockables.SHULKER_BOX");
		case BROWN_SHULKER_BOX:
			return Config.getBoolean("lockables.SHULKER_BOX");
		case RED_SHULKER_BOX:
			return Config.getBoolean("lockables.SHULKER_BOX");
		case ORANGE_SHULKER_BOX:
			return Config.getBoolean("lockables.SHULKER_BOX");
		case YELLOW_SHULKER_BOX:
			return Config.getBoolean("lockables.SHULKER_BOX");
		case LIME_SHULKER_BOX:
			return Config.getBoolean("lockables.SHULKER_BOX");
		case GREEN_SHULKER_BOX:
			return Config.getBoolean("lockables.SHULKER_BOX");
		case CYAN_SHULKER_BOX:
		    return Config.getBoolean("lockables.SHULKER_BOX");
		case LIGHT_BLUE_SHULKER_BOX:
		    return Config.getBoolean("lockables.SHULKER_BOX");
		case BLUE_SHULKER_BOX:
		    return Config.getBoolean("lockables.SHULKER_BOX");
		case PURPLE_SHULKER_BOX:
		    return Config.getBoolean("lockables.SHULKER_BOX");
		case MAGENTA_SHULKER_BOX:
		    return Config.getBoolean("lockables.SHULKER_BOX");
		case PINK_SHULKER_BOX:
		    return Config.getBoolean("lockables.SHULKER_BOX");
		case SMOKER:
			return Config.getBoolean("lockables.SMOKER");
		case TRAPPED_CHEST:
			return Config.getBoolean("lockables.TRAPPED_CHEST");
		case OAK_DOOR:
			return Config.getBoolean("lockables-doors.OAK_DOOR");
		case SPRUCE_DOOR:
			return Config.getBoolean("lockables-doors.SPRUCE_DOOR");
		case BIRCH_DOOR:
			return Config.getBoolean("lockables-doors.BIRCH_DOOR");
		case JUNGLE_DOOR:
			return Config.getBoolean("lockables-doors.JUNGLE_DOOR");
		case ACACIA_DOOR:
			return Config.getBoolean("lockables-doors.ACACIA_DOOR");
		case DARK_OAK_DOOR:
			return Config.getBoolean("lockables-doors.DARK_OAK_DOOR");
		case MANGROVE_DOOR:
			return Config.getBoolean("lockables-doors.MANGROVE_DOOR");
		case CHERRY_DOOR:
			return Config.getBoolean("lockables-doors.CHERRY_DOOR");
		case BAMBOO_DOOR:
			return Config.getBoolean("lockables-doors.BAMBOO_DOOR");
		case CRIMSON_DOOR:
			return Config.getBoolean("lockables-doors.CRIMSON_DOOR");
		case WARPED_DOOR:
			return Config.getBoolean("lockables-doors.WARPED_DOOR");
		case IRON_DOOR:
			return Config.getBoolean("lockables-doors.IRON_DOOR");
		case COPPER_DOOR:
			return Config.getBoolean("lockables-doors.COPPER_DOOR");
		case EXPOSED_COPPER_DOOR:
			return Config.getBoolean("lockables-doors.EXPOSED_COPPER_DOOR");
		case WEATHERED_COPPER_DOOR:
			return Config.getBoolean("lockables-doors.WEATHERED_COPPER_DOOR");
		case OXIDIZED_COPPER_DOOR:
			return Config.getBoolean("lockables-doors.OXIDIZED_COPPER_DOOR");
		case WAXED_COPPER_DOOR:
			return Config.getBoolean("lockables-doors.WAXED_COPPER_DOOR");
		case WAXED_EXPOSED_COPPER_DOOR:
			return Config.getBoolean("lockables-doors.WAXED_EXPOSED_COPPER_DOOR");
		case WAXED_WEATHERED_COPPER_DOOR:
			return Config.getBoolean("lockables-doors.WAXED_WEATHERED_COPPER_DOOR");
		case WAXED_OXIDIZED_COPPER_DOOR:
			return Config.getBoolean("lockables-doors.WAXED_OXIDIZED_COPPER_DOOR");
		default:
			return false;
		}
	}

	public static List<Block> getConnectedChestBlocks(Block block) {
		Set<Block> blocks = new LinkedHashSet<>();

		if (!(block.getState() instanceof Chest)) {
			blocks.add(block);
			return new ArrayList<>(blocks);
		}

		Chest chest = (Chest) block.getState();
		InventoryHolder holder = chest.getInventory().getHolder();

		if (holder instanceof DoubleChest) {
			DoubleChest doubleChest = (DoubleChest) holder;
			addHolderBlock(doubleChest.getLeftSide(), blocks);
			addHolderBlock(doubleChest.getRightSide(), blocks);
			if (blocks.isEmpty()) {
				blocks.add(block);
			}
		} else {
			blocks.add(block);
		}

		return new ArrayList<>(blocks);
	}

	private static void addHolderBlock(InventoryHolder holder, Set<Block> blocks) {
		if (holder instanceof BlockState) {
			blocks.add(((BlockState) holder).getBlock());
		}
	}
}
