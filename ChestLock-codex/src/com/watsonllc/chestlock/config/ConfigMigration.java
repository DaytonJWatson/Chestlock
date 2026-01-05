package com.watsonllc.chestlock.config;

import org.bukkit.configuration.file.FileConfiguration;

import com.watsonllc.chestlock.Main;
import com.watsonllc.chestlock.logic.HopperCache;

public class ConfigMigration {

        private static final String TARGET_VERSION = "1.4.0";

        public static boolean ensureCurrentConfig() {
                FileConfiguration config = Main.instance.getConfig();
                Object currentVersion = config.get("configVersion");

                if (TARGET_VERSION.equals(currentVersion)) {
                        backfillNewSettings(config);
                        return true;
                }

                if (currentVersion == null || "1.3.0".equals(currentVersion)) {
                        migrateFrom130(config);
                        return true;
                }

                logIncompatibleConfig(currentVersion);
                return false;
        }

        private static void migrateFrom130(FileConfiguration config) {
                backfillNewSettings(config);
                config.set("configVersion", TARGET_VERSION);
                Main.instance.saveConfig();
                HopperCache.invalidate();
        }

        private static void backfillNewSettings(FileConfiguration config) {
                if (!config.isSet("settings.untagged-hoppers")) {
                        config.set("settings.untagged-hoppers", "ALLOW");
                        Main.instance.saveConfig();
                }

                if (!config.isSet("settings.prefix")) {
                        config.set("settings.prefix", "&8[&6ChestLock&8] &7 ");
                        Main.instance.saveConfig();
                }
        }

        private static void logIncompatibleConfig(Object currentVersion) {
                Main.instance.getLogger().severe("#########################################################");
                Main.instance.getLogger().severe("#        ChestLock found conflicting configurations     #");
                Main.instance.getLogger().severe("#            You NEED to delete your config.yml         #");
                Main.instance.getLogger().severe("#                 and GENERATE a NEW one!               #");
                Main.instance.getLogger().severe("#         need help? Discord: discord.gg/BGurTEm2nj     #");
                Main.instance.getLogger().severe("#  Unknown config version found: " + currentVersion);
                Main.instance.getLogger().severe("#########################################################");
        }
}
