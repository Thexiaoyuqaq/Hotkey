// ========== Hotkey.java - 主插件类 ==========
/*
 * Copyright (c) 2025 Thexiaoyu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package Thexiaoyu.keyCommand;

import Thexiaoyu.keyCommand.commands.HotkeyCommand;
import Thexiaoyu.keyCommand.commands.HotkeyTabCompleter;
import Thexiaoyu.keyCommand.listeners.HotkeyListener;
import Thexiaoyu.keyCommand.managers.*;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.logging.Level;

public class Hotkey extends JavaPlugin {

    private LangManager langManager;
    private HotkeyManager hotkeyManager;
    private PermissionManager permissionManager;
    private CooldownManager cooldownManager;
    private EffectManager effectManager;

    @Override
    public void onEnable() {
        try {
            saveDefaultConfig();

            langManager = new LangManager(this);
            permissionManager = new PermissionManager(this);
            cooldownManager = new CooldownManager(this);
            effectManager = new EffectManager(this);
            hotkeyManager = new HotkeyManager(this);

            getServer().getPluginManager().registerEvents(new HotkeyListener(this), this);

            getCommand("hotkey").setExecutor(new HotkeyCommand(this));
            getCommand("hotkey").setTabCompleter(new HotkeyTabCompleter());

            startCleanupTask();

            validateDoubleSneakConfig();

            getLogger().info(ChatColor.translateAlternateColorCodes('&',
                    langManager.getMessage("prefix")) + "插件已启用! v" + getDescription().getVersion());
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "插件启动失败!", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);

        getLogger().info(ChatColor.translateAlternateColorCodes('&',
                langManager.getMessage("prefix")) + "插件已禁用!");
    }

    /**
     * 启动定期清理任务
     */
    private void startCleanupTask() {
        long cleanupInterval = getConfig().getLong("settings.cleanup-interval", 600) * 20L; // 转换为 ticks

        new BukkitRunnable() {
            @Override
            public void run() {
                if (cooldownManager != null) {
                    cooldownManager.cleanup();
                }
            }
        }.runTaskTimer(this, cleanupInterval, cleanupInterval);
    }

    /**
     * 重载插件配置
     */
    public void reload() {
        reloadConfig();
        langManager.loadLang();
        permissionManager.reload();
        effectManager.reload();
        hotkeyManager.reload();
    }

    /**
     * 验证 double_sneak 配置
     */
    private void validateDoubleSneakConfig() {
        int maxDoubleSneakCount = 0;
        ConfigurationSection hotkeys = getConfig().getConfigurationSection("hotkeys");

        if (hotkeys != null) {
            for (String key : hotkeys.getKeys(false)) {
                if (key.startsWith("double_sneak_")) {
                    try {
                        int count = Integer.parseInt(key.substring(13));
                        if (count > maxDoubleSneakCount) {
                            maxDoubleSneakCount = count;
                        }

                        // 警告不合理的配置!!!
                        if (count > 10) {
                            getLogger().warning("检测到 " + key + " 配置，按键次数可能过多，建议不超过 10 次");
                        }
                    } catch (NumberFormatException e) {
                        getLogger().warning("无效的热键配置: " + key);
                    }
                }
            }
        }

        if (maxDoubleSneakCount > 0) {
            getLogger().info("已启用多次 Shift 功能，最大次数: " + maxDoubleSneakCount);
        }
    }

    public LangManager getLangManager() { return langManager; }
    public HotkeyManager getHotkeyManager() { return hotkeyManager; }
    public PermissionManager getPermissionManager() { return permissionManager; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public EffectManager getEffectManager() { return effectManager; }
}