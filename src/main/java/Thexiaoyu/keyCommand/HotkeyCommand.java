/*
 * Copyright (c) 2024 Thexiaoyu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package Thexiaoyu.keyCommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import java.util.Map;

public class HotkeyCommand implements CommandExecutor {
    
    private final Hotkey plugin;
    
    public HotkeyCommand(Hotkey plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        if (!sender.hasPermission("hotkey.admin")) {
            sender.sendMessage(plugin.getLangManager().getMessage("no_permission"));
        }
        
        switch (args[0].toLowerCase()) {
            case "list":
                showHotkeyList(sender);
                break;
            case "reload":
                    plugin.reloadConfig();
                    plugin.getLangManager().loadLang();
                    sender.sendMessage(plugin.getLangManager().getMessage("reload"));
                break;
            default:
                showHelp(sender);
                break;
        }
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(plugin.getLangManager().getMessage("help.title"));
        sender.sendMessage(plugin.getLangManager().getMessage("help.list"));
        if (sender.hasPermission("hotkey.admin")) {
            sender.sendMessage(plugin.getLangManager().getMessage("help.reload"));
        }
    }
    
    private void showHotkeyList(CommandSender sender) {
        ConfigurationSection hotkeys = plugin.getConfig().getConfigurationSection("hotkeys");
        if (hotkeys == null) {
            sender.sendMessage(plugin.getLangManager().getMessage("list.empty"));
            return;
        }
        
        sender.sendMessage(plugin.getLangManager().getMessage("list.title"));
        for (String key : hotkeys.getKeys(false)) {
            ConfigurationSection hotkeySection = hotkeys.getConfigurationSection(key);
            if (hotkeySection != null) {
                sender.sendMessage(plugin.getLangManager().getMessage("list.format", 
                    Map.of("hotkey", key)));
                for (String cmd : hotkeySection.getStringList("commands")) {
                    sender.sendMessage(plugin.getLangManager().getMessage("list.command",
                        Map.of("command", cmd)));
                }
            }
        }
    }
} 