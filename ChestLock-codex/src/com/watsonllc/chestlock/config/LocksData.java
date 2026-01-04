package com.watsonllc.chestlock.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.watsonllc.chestlock.Main;

public class LocksData {
	
	private static File locksFile = new File(Main.instance.getDataFolder(), "locks.yml");
	private static YamlConfiguration locks = YamlConfiguration.loadConfiguration(locksFile);
	
	public static void create() {
		if(locksFile.exists())
			return;
		else
			save();
	}
	
	public static void save() {
		try {
			locks.save(locksFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Object get(String string) {
		return locks.get(string);
	}

	// dont forget to save();
	public static void set(String string, Object object) {
		locks.set(string, object);
	}

        public static YamlConfiguration getConfiguration() {
                return locks;
        }
	
	public static List<String> retrieveSubSections(String sectionPath) {
        List<String> subSections = new ArrayList<>();

        ConfigurationSection section = locks.getConfigurationSection(sectionPath);

        if (section != null) {
            Set<String> keys = section.getKeys(false);
            for (String key : keys) {
                if (locks.isConfigurationSection(sectionPath + "." + key)) {
                    subSections.add(key);
                }
            }
        }

        return subSections;
    }
	
	public static Set<String> retrieveSubSectionNames(String sectionPath) {

        // Get the specified section
        ConfigurationSection section = locks.getConfigurationSection(sectionPath);

        if (section != null) {
            // Get the names of all sub-sections
            return section.getKeys(false);
        }

        return null; // or an empty set if section is null
	}
}
