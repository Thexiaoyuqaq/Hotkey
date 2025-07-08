/*
 * Copyright (c) 2024 Thexiaoyu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package Thexiaoyu.keyCommand.managers;

import Thexiaoyu.keyCommand.Hotkey;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LangManager {
    private final Hotkey plugin;
    private FileConfiguration langConfig;
    private final Map<String, String> messages = new HashMap<>();
    
    public LangManager(Hotkey plugin) {
        this.plugin = plugin;
        loadLang();
    }
    
    public void loadLang() {
        File langFile = new File(plugin.getDataFolder(), "lang.yml");
        if (!langFile.exists()) {
            plugin.saveResource("lang.yml", false);
        }
        
        langConfig = YamlConfiguration.loadConfiguration(langFile);
        loadMessages("messages");
    }
    
    private void loadMessages(String path) {
        for (String key : langConfig.getConfigurationSection(path).getKeys(true)) {
            String fullPath = path + "." + key;
            if (langConfig.isString(fullPath)) {
                String message = langConfig.getString(fullPath);
                messages.put(key, message);
            }
        }
    }
    
    public String getMessage(String key) {
        String message = messages.getOrDefault(key, "&c未找到语言键: " + key);
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public String getMessage(String key, Map<String, String> placeholders) {
        String message = getMessage(key); 
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }
} 