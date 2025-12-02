package com.watsonllc.chestlock.logic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.watsonllc.chestlock.config.Config;

public class ChestSorter {

    private static final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_TIME = 1000;

    public static void sortByAlphabetical(InventoryClickEvent event) {
        if (!isValidEvent(event)) return;

        Inventory inventory = event.getInventory();
        ItemStack[] items = inventory.getContents();

        Arrays.sort(items, (a, b) -> {
            if (a == null) return 1;
            if (b == null) return -1;
            return a.getType().name().compareTo(b.getType().name());
        });

        inventory.setContents(items);
        event.setCancelled(true);
    }

    public static void sortByType(InventoryClickEvent event) {
        if (!isValidEvent(event)) return;

        Inventory inventory = event.getInventory();
        ItemStack[] items = inventory.getContents();

        Arrays.sort(items, (a, b) -> {
            if (a == null && b == null) return 0;
            if (a == null) return 1;
            if (b == null) return -1;
            return a.getType().compareTo(b.getType());
        });

        inventory.setContents(items);
        event.setCancelled(true);
    }

    private static boolean isValidEvent(InventoryClickEvent event) {
        if (event.getClick() != ClickType.valueOf(Config.getString("settings.sortWith"))) return false;

        String inventoryType = event.getInventory().getType().name();
        if (!isSortableInventory(inventoryType)) return false;
        
        UUID playerId = event.getWhoClicked().getUniqueId();
        long currentTime = System.currentTimeMillis();
        if (cooldowns.containsKey(playerId)) {
            long lastSortTime = cooldowns.get(playerId);
            if (currentTime - lastSortTime < COOLDOWN_TIME) {
                return false;
            }
        }
        cooldowns.put(playerId, currentTime);

        return true;
    }

    private static boolean isSortableInventory(String inventoryType) {
        return inventoryType.equals("CHEST") || inventoryType.equals("PLAYER");
    }
}
