package com.watsonllc.chestlock.config;

import com.watsonllc.chestlock.Main;
import com.watsonllc.chestlock.Utils;

public class Config {
	
	public static void setup() {
		createConfig();
	}
	
	public static void createConfig() {
		Main.instance.getConfig().options().copyDefaults();
		Main.instance.saveDefaultConfig();
	}
	
	public static void save() {
		Main.instance.saveConfig();
	}
	
	public static void reload() {
		Main.instance.reloadConfig();
	}
	
	public static String getConfigVersion() {
		return (String) Main.instance.getConfig().get("configVersion");
	}
	
	public static int getInt(String string) {
		return Main.instance.getConfig().getInt(string);
	}
	
	public static String getString(String string) {
		String value = Main.instance.getConfig().getString(string);

		if (value == null)
			return "";

		if (string.startsWith("messages.")) {
			String prefix = Main.instance.getConfig().getString("settings.prefix", "");

			if (prefix != null && !prefix.isEmpty() && !value.startsWith(prefix))
				value = prefix + value;
		}

		return Utils.color(value);
	}

        public static String getStringRaw(String string) {
                return Main.instance.getConfig().getString(string);
        }
	
	public static Boolean getBoolean(String string) {
		return Main.instance.getConfig().getBoolean(string);
	}
	
}
