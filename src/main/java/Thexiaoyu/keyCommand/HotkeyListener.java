/*
 * Copyright (c) 2024 Thexiaoyu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package Thexiaoyu.keyCommand;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.block.Action;
import org.bukkit.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import Thexiaoyu.keyCommand.conditions.ConditionChecker;

public class HotkeyListener implements Listener {
    
    private final Hotkey plugin;
    private final HashMap<UUID, Long> lastSneak = new HashMap<>();
    private final HashMap<UUID, HashMap<String, Long>> cooldowns = new HashMap<>();
    private static final long COMBO_TIMEOUT = 500;
    private static final long CLEANUP_INTERVAL = 30000;
    private long lastCleanup = System.currentTimeMillis();
    
    private final Map<UUID, Long> lastSneakRelease = new HashMap<>();
    
    private final ConditionChecker conditionChecker;
    
    public HotkeyListener(Hotkey plugin) {
        this.plugin = plugin;
        this.conditionChecker = new ConditionChecker(plugin);
    }
    
    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (event.isSneaking()) {
            lastSneak.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }
    
    @EventHandler
    public void onPlayerSwapHand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        cleanupOldData();
        
        if (lastSneak.containsKey(playerId)) {
            long lastSneakTime = lastSneak.get(playerId);
            long currentTime = System.currentTimeMillis();
            
            if (currentTime - lastSneakTime <= COMBO_TIMEOUT) {
                executeHotkey(player, "shift_f");
                event.setCancelled(true);
            }
            
            lastSneak.remove(playerId);
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.isSneaking()) {
            String hotkeyId = null;
            if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                hotkeyId = "shift_left";
            } else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                hotkeyId = "shift_right";
            }
            if (hotkeyId != null) {
                executeHotkey(player, hotkeyId);
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (player.isSneaking()) {
            executeHotkey(player, "shift_q");
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        cleanupOldData();
        
        if (!event.isSneaking()) {
            if (lastSneakRelease.containsKey(playerId)) {
                long lastRelease = lastSneakRelease.get(playerId);
                if (currentTime - lastRelease <= 300) {
                    executeHotkey(player, "double_sneak");
                    lastSneakRelease.remove(playerId);
                    return;
                }
            }
            lastSneakRelease.put(playerId, currentTime);
        }
    }
    
    private void executeHotkey(Player player, String hotkeyId) {
        ConfigurationSection hotkeyConfig = plugin.getConfig().getConfigurationSection("hotkeys." + hotkeyId);
        if (hotkeyConfig == null) return;
        
        if (!checkConditions(player, hotkeyId, hotkeyConfig)) return;
        
        List<String> commands = hotkeyConfig.getStringList("commands");
        for (String command : commands) {
            command = command.replace("%player%", player.getName());
            
            if (command.startsWith("[cmd]")) {
                String consoleCommand = command.substring(5).trim();
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), consoleCommand);
            } else {
                player.performCommand(command);
            }
        }

        playEffects(player, hotkeyId, hotkeyConfig);
        setCooldown(player, hotkeyId);
    }
    
    private boolean checkConditions(Player player, String hotkeyId, ConfigurationSection config) {
        if (plugin.getConfig().getBoolean("settings.cooldown.enabled", true) && isOnCooldown(player, hotkeyId)) {
            long remainingTime = getCooldownTime(player, hotkeyId);
            player.sendMessage(plugin.getLangManager().getMessage("cooldown", 
                Map.of("time", String.valueOf(remainingTime))));
            return false;
        }
 
        return conditionChecker.checkAll(player, hotkeyId, config);
    }
    
    private void playEffects(Player player, String hotkeyId, ConfigurationSection config) {
        Location loc = player.getLocation();
        ConfigurationSection globalEffects = plugin.getConfig().getConfigurationSection("settings.effects");

        if (globalEffects.getBoolean("sound.enabled", true)) {
            String soundName = config.getString("effects.sound", 
                globalEffects.getString("sound.type"));
            float volume = (float) globalEffects.getDouble("sound.volume", 1.0);
            float pitch = (float) globalEffects.getDouble("sound.pitch", 1.0);
            
            try {
                Sound sound = Sound.valueOf(soundName);
                player.playSound(loc, sound, volume, pitch);
            } catch (IllegalArgumentException ignored) {}
        }
        
        if (globalEffects.getBoolean("particles.enabled", true)) {
            String particleName = config.getString("effects.particle",
                globalEffects.getString("particles.type"));
            try {
                Particle particle = Particle.valueOf(particleName);
                double spread = globalEffects.getDouble("particles.spread", 0.5);
                int count = globalEffects.getInt("particles.count", 10);
                player.getWorld().spawnParticle(particle, loc.add(0, 1, 0), count, spread, spread, spread, 0);
            } catch (IllegalArgumentException ignored) {}
        }

        String message = config.getString("effects.message");
        if (message != null) {
            if (plugin.getConfig().getBoolean("settings.messages.chat", true)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            }
            if (plugin.getConfig().getBoolean("settings.messages.actionbar", true)) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                    new TextComponent(ChatColor.translateAlternateColorCodes('&', message)));
            }
        }
    }
    
    private void setCooldown(Player player, String hotkeyId) {
        UUID playerId = player.getUniqueId();
        cooldowns.computeIfAbsent(playerId, k -> new HashMap<>());
        cooldowns.get(playerId).put(hotkeyId, System.currentTimeMillis());
    }
    
    private boolean isOnCooldown(Player player, String hotkeyId) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) return false;
        
        HashMap<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (!playerCooldowns.containsKey(hotkeyId)) return false;
        
        long lastUse = playerCooldowns.get(hotkeyId);
        double cooldownTime = plugin.getConfig().getDouble("hotkeys." + hotkeyId + ".conditions.cooldown",
            plugin.getConfig().getDouble("settings.cooldown", 3)) * 1000;
            
        return System.currentTimeMillis() - lastUse < cooldownTime;
    }
    
    private long getCooldownTime(Player player, String hotkeyId) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) return 0;
        
        HashMap<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (!playerCooldowns.containsKey(hotkeyId)) return 0;
        
        long lastUse = playerCooldowns.get(hotkeyId);
        double cooldownTime = plugin.getConfig().getDouble("hotkeys." + hotkeyId + ".conditions.cooldown",
            plugin.getConfig().getDouble("settings.cooldown", 3)) * 1000;
            
        long remaining = (long) ((lastUse + cooldownTime - System.currentTimeMillis()) / 1000);
        return Math.max(0, remaining);
    }
    
    private void cleanupOldData() {
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastCleanup < CLEANUP_INTERVAL) {
            return;
        }
        
        lastCleanup = currentTime;
        
        lastSneak.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > COMBO_TIMEOUT);
            
        lastSneakRelease.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > 1000);
            
        cooldowns.forEach((playerId, playerCooldowns) -> {
            playerCooldowns.entrySet().removeIf(entry -> {
                String hotkeyId = entry.getKey();
                double cooldownTime = plugin.getConfig().getDouble(
                    "hotkeys." + hotkeyId + ".conditions.cooldown",
                    plugin.getConfig().getDouble("settings.cooldown", 3)) * 1000;
                return currentTime - entry.getValue() > cooldownTime;
            });
        });
        cooldowns.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
} 