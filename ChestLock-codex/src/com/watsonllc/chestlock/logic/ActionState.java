package com.watsonllc.chestlock.logic;

public class ActionState {
    private final boolean toggleEnabled;
    private final String target;

    public ActionState(boolean toggleEnabled, String target) {
        this.toggleEnabled = toggleEnabled;
        this.target = target;
    }

    public boolean isToggleEnabled() {
        return toggleEnabled;
    }

    public String getTarget() {
        return target;
    }
}
