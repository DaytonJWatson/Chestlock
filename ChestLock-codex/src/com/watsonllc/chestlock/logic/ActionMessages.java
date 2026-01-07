package com.watsonllc.chestlock.logic;

import com.watsonllc.chestlock.config.Config;

public final class ActionMessages {
    private ActionMessages() {
    }

    public static String getActionName(PlayerActionType type) {
        switch (type) {
        case CLAIM_LOCK:
            return "Claim";
        case DESTROY_LOCK:
            return "Destroy";
        case MAKE_PUBLIC:
            return "Public";
        case ADD_OWNER:
            return "Add owner";
        case REMOVE_OWNER:
            return "Remove owner";
        default:
            return "Action";
        }
    }

    public static String getModeStart(PlayerActionType type, String target) {
        switch (type) {
        case CLAIM_LOCK:
            return Config.getString("messages.modeClaim");
        case DESTROY_LOCK:
            return Config.getString("messages.modeDestroy");
        case MAKE_PUBLIC:
            return Config.getString("messages.modePublic");
        case ADD_OWNER:
            return replaceTarget(Config.getString("messages.modeAddOwner"), target);
        case REMOVE_OWNER:
            return replaceTarget(Config.getString("messages.modeRemoveOwner"), target);
        default:
            return "";
        }
    }

    public static String getActionBar(PlayerActionType type, String target) {
        switch (type) {
        case CLAIM_LOCK:
            return colorRaw(Config.getStringRaw("messages.actionbarClaim"));
        case DESTROY_LOCK:
            return colorRaw(Config.getStringRaw("messages.actionbarDestroy"));
        case MAKE_PUBLIC:
            return colorRaw(Config.getStringRaw("messages.actionbarPublic"));
        case ADD_OWNER:
            return replaceTarget(colorRaw(Config.getStringRaw("messages.actionbarAddOwner")), target);
        case REMOVE_OWNER:
            return replaceTarget(colorRaw(Config.getStringRaw("messages.actionbarRemoveOwner")), target);
        default:
            return "";
        }
    }

    public static String getNextStep(PlayerActionType type, String target) {
        switch (type) {
        case CLAIM_LOCK:
            return colorRaw(Config.getStringRaw("messages.nextClaim"));
        case DESTROY_LOCK:
            return colorRaw(Config.getStringRaw("messages.nextDestroy"));
        case MAKE_PUBLIC:
            return colorRaw(Config.getStringRaw("messages.nextPublic"));
        case ADD_OWNER:
            return replaceTarget(colorRaw(Config.getStringRaw("messages.nextAddOwner")), target);
        case REMOVE_OWNER:
            return replaceTarget(colorRaw(Config.getStringRaw("messages.nextRemoveOwner")), target);
        default:
            return "";
        }
    }

    private static String replaceTarget(String message, String target) {
        if (target == null)
            return message;
        return message.replace("%target%", target);
    }

    private static String colorRaw(String message) {
        return message == null ? "" : com.watsonllc.chestlock.Utils.color(message);
    }
}
