package com.watsonllc.chestlock.logic;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.watsonllc.chestlock.Main;
import com.watsonllc.chestlock.config.Config;

public final class PlayerStateManager {
    private static final Map<UUID, PlayerState> STATES = new HashMap<>();

    private PlayerStateManager() {
    }

    public static boolean hasAction(Player player, PlayerActionType type) {
        return getState(player).hasAction(type);
    }

    public static ActionState getAction(Player player, PlayerActionType type) {
        return getState(player).getAction(type);
    }

    public static boolean isToggleEnabled(Player player, PlayerActionType type) {
        ActionState state = getAction(player, type);
        return state != null && state.isToggleEnabled();
    }

    public static void startAction(Player player, PlayerActionType type, boolean toggle, String target) {
        getState(player).startAction(type, new ActionState(toggle, target));
    }

    public static void clearAction(Player player, PlayerActionType type) {
        PlayerState state = getState(player);
        state.clearAction(type);
        cleanupIfIdle(player, state);
    }

    public static void scheduleTimeout(Player player, PlayerActionType type, String actionName) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!hasAction(player, type)) {
                    return;
                }

                clearAction(player, type);
                String commandTimeout = Config.getString("messages.commandTimeout");
                commandTimeout = commandTimeout.replace("%action%", actionName);
                player.sendMessage(commandTimeout);
            }
        }.runTaskLater(Main.instance, 20 * 15);
    }

    public static boolean isBypassing(Player player) {
        return getState(player).isBypassing();
    }

    public static void setBypassing(Player player, boolean bypassing) {
        PlayerState state = getState(player);
        state.setBypassing(bypassing);

        if (!bypassing) {
            state.setBypassWarned(false);
            cleanupIfIdle(player, state);
        }
    }

    public static boolean hasSeenBypassWarning(Player player) {
        return getState(player).hasSeenBypassWarning();
    }

    public static void markBypassWarned(Player player) {
        getState(player).setBypassWarned(true);
    }

    public static void resetBypassWarning(Player player) {
        getState(player).setBypassWarned(false);
    }

    private static PlayerState getState(Player player) {
        return STATES.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerState());
    }

    private static void cleanupIfIdle(Player player, PlayerState state) {
        if (state.isIdle()) {
            STATES.remove(player.getUniqueId());
        }
    }

    private static final class PlayerState {
        private final EnumMap<PlayerActionType, ActionState> actions = new EnumMap<>(PlayerActionType.class);
        private boolean bypassing;
        private boolean bypassWarned;

        public boolean hasAction(PlayerActionType type) {
            return actions.containsKey(type);
        }

        public ActionState getAction(PlayerActionType type) {
            return actions.get(type);
        }

        public void startAction(PlayerActionType type, ActionState state) {
            actions.put(type, state);
        }

        public void clearAction(PlayerActionType type) {
            actions.remove(type);
        }

        public boolean isBypassing() {
            return bypassing;
        }

        public void setBypassing(boolean bypassing) {
            this.bypassing = bypassing;
        }

        public boolean hasSeenBypassWarning() {
            return bypassWarned;
        }

        public void setBypassWarned(boolean bypassWarned) {
            this.bypassWarned = bypassWarned;
        }

        public boolean isIdle() {
            return actions.isEmpty() && !bypassing && !bypassWarned;
        }
    }
}
