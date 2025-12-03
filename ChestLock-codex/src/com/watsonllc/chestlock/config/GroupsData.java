package com.watsonllc.chestlock.config;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;

import com.watsonllc.chestlock.Main;

public class GroupsData {

        private static final File GROUPS_FILE = new File(Main.instance.getDataFolder(), "groups.yml");
        private static final YamlConfiguration GROUPS = YamlConfiguration.loadConfiguration(GROUPS_FILE);

        public static void create() {
                if (!Main.instance.getDataFolder().exists())
                        Main.instance.getDataFolder().mkdirs();

                if (GROUPS_FILE.exists())
                        return;

                save();
        }

        public static void save() {
                try {
                        GROUPS.save(GROUPS_FILE);
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        public static Object get(String path) {
                return GROUPS.get(path);
        }

        public static void set(String path, Object value) {
                GROUPS.set(path, value);
        }

        public static boolean contains(String path) {
                return GROUPS.contains(path);
        }

        public static YamlConfiguration getConfiguration() {
                return GROUPS;
        }
}
