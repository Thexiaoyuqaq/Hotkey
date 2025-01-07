/*
 * Copyright (c) 2024 Thexiaoyu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package Thexiaoyu.keyCommand.conditions;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import Thexiaoyu.keyCommand.Hotkey;

import java.util.List;
import java.util.Map;

public class ConditionChecker {
    private final Hotkey plugin;
    
    public ConditionChecker(Hotkey plugin) {
        this.plugin = plugin;
    }
    
    public boolean checkExpLevel(Player player, int required) {
        if (required > 0 && player.getLevel() < required) {
            player.sendMessage(plugin.getLangManager().getMessage("condition.exp_level", 
                Map.of("level", String.valueOf(required))));
            return false;
        }
        return true;
    }
    
    public boolean checkFoodLevel(Player player, int required) {
        if (required > 0 && player.getFoodLevel() < required) {
            player.sendMessage(plugin.getLangManager().getMessage("condition.food_level", 
                Map.of("level", String.valueOf(required))));
            return false;
        }
        return true;
    }
    
    public boolean checkGameMode(Player player, String gamemode) {
        if (gamemode != null) {
            try {
                GameMode requiredMode = GameMode.valueOf(gamemode);
                if (player.getGameMode() != requiredMode) {
                    player.sendMessage(plugin.getLangManager().getMessage("condition.gamemode", 
                        Map.of("gamemode", gamemode)));
                    return false;
                }
            } catch (IllegalArgumentException ignored) {}
        }
        return true;
    }
    
    public boolean checkHasItem(Player player, String itemName) {
        if (itemName != null) {
            try {
                Material material = Material.valueOf(itemName);
                if (!player.getInventory().contains(material)) {
                    player.sendMessage(plugin.getLangManager().getMessage("condition.has_item", 
                        Map.of("item", itemName)));
                    return false;
                }
            } catch (IllegalArgumentException ignored) {}
        }
        return true;
    }
    
    public boolean checkInWorld(Player player, String worldName) {
        if (worldName != null && !player.getWorld().getName().equals(worldName)) {
            player.sendMessage(plugin.getLangManager().getMessage("condition.in_world", 
                Map.of("world", worldName)));
            return false;
        }
        return true;
    }
    
    public boolean checkTimeBetween(Player player, List<Integer> timeRange) {
        if (timeRange != null && timeRange.size() == 2) {
            long worldTime = player.getWorld().getTime();
            int start = timeRange.get(0);
            int end = timeRange.get(1);
            if (worldTime < start || worldTime > end) {
                player.sendMessage(plugin.getLangManager().getMessage("condition.time_between", 
                    Map.of("start", String.valueOf(start), "end", String.valueOf(end))));
                return false;
            }
        }
        return true;
    }
    
    public boolean checkAllRequirements(Player player, ConfigurationSection requirements) {
        if (requirements == null) return true;
        
        return checkExpLevel(player, requirements.getInt("exp_level", 0))
            && checkFoodLevel(player, requirements.getInt("food_level", 0))
            && checkGameMode(player, requirements.getString("gamemode"))
            && checkHasItem(player, requirements.getString("has_item"))
            && checkInWorld(player, requirements.getString("in_world"))
            && checkTimeBetween(player, requirements.getIntegerList("time_between"));
    }

    public boolean checkAll(Player player, String hotkeyId, ConfigurationSection config) {
        ConfigurationSection conditions = config.getConfigurationSection("conditions");
        if (conditions == null) return true;
        
        // 检查是否启用
        if (!config.getBoolean("enabled", true)) {
            return false;
        }
        
        // 检查权限
        String permission = conditions.getString("permission");
        if (permission != null && !player.hasPermission(permission)) {
            player.sendMessage(plugin.getLangManager().getMessage("no_permission"));
            return false;
        }

        return checkAllRequirements(player, conditions.getConfigurationSection("requirements"));
    }
} 