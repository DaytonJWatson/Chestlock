package com.watsonllc.chestlock.logic;

public enum UntaggedHopperPolicy {
        ALLOW,
        DENY,
        TAG_ON_USE;

        public static UntaggedHopperPolicy fromConfig(String value) {
                if (value == null)
                        return ALLOW;

                try {
                        return UntaggedHopperPolicy.valueOf(value.trim().toUpperCase());
                } catch (IllegalArgumentException ex) {
                        return ALLOW;
                }
        }
}
