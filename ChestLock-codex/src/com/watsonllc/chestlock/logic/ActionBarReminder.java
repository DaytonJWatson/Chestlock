package com.watsonllc.chestlock.logic;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.watsonllc.chestlock.Main;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public final class ActionBarReminder {
    private static final long REMINDER_INTERVAL_TICKS = 40L;

    private ActionBarReminder() {
    }

    public static void start() {
        Bukkit.getScheduler().runTaskTimer(Main.instance, () -> {
            PlayerStateManager.forEachActiveAction(ActionBarReminder::sendReminder);
        }, REMINDER_INTERVAL_TICKS, REMINDER_INTERVAL_TICKS);
    }

    private static void sendReminder(UUID playerId, PlayerActionType type, ActionState state) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline())
            return;

        String message = ActionMessages.getActionBar(type, state == null ? null : state.getTarget());
        if (message == null || message.isEmpty())
            return;

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }
}
