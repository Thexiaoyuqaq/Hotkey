/*
 * Copyright (c) 2024 Thexiaoyu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package Thexiaoyu.keyCommand;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Hotkey extends JavaPlugin {
    
    private LangManager langManager;
    
    @Override
    public void onEnable() {
        langManager = new LangManager(this);
        
        getServer().getPluginManager().registerEvents(new HotkeyListener(this), this);
        
        getCommand("hotkey").setExecutor(new HotkeyCommand(this));
        getCommand("hotkey").setTabCompleter(new HotkeyTabCompleter());
        
        saveDefaultConfig();
        getLogger().info(ChatColor.translateAlternateColorCodes('&', langManager.getMessage("prefix")) + "插件已启用!");
    }

    @Override
    public void onDisable() {
        getLogger().info(ChatColor.translateAlternateColorCodes('&', langManager.getMessage("prefix")) + "插件已禁用!");
    }
    
    public LangManager getLangManager() {
        return langManager;
    }
} 