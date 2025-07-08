package Thexiaoyu.keyCommand.managers;

import Thexiaoyu.keyCommand.Hotkey;
import Thexiaoyu.keyCommand.HotkeyConfig;
import Thexiaoyu.keyCommand.conditions.ConditionChecker;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class HotkeyManager {

    private final Hotkey plugin;
    private final Map<String, HotkeyConfig> hotkeyConfigs = new ConcurrentHashMap<>();
    private final ConditionChecker conditionChecker;

    public HotkeyManager(Hotkey plugin) {
        this.plugin = plugin;
        this.conditionChecker = new ConditionChecker(plugin);
        loadHotkeys();
    }

    /**
     * 加载所有热键配置
     */
    private void loadHotkeys() {
        hotkeyConfigs.clear();

        ConfigurationSection hotkeysSection = plugin.getConfig().getConfigurationSection("hotkeys");
        if (hotkeysSection == null) return;

        for (String hotkeyId : hotkeysSection.getKeys(false)) {
            try {
                ConfigurationSection hotkeySection = hotkeysSection.getConfigurationSection(hotkeyId);
                if (hotkeySection != null) {
                    HotkeyConfig config = new HotkeyConfig(hotkeyId, hotkeySection);
                    hotkeyConfigs.put(hotkeyId, config);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "无法加载热键: " + hotkeyId, e);
            }
        }

        plugin.getLogger().info("已加载 " + hotkeyConfigs.size() + " 个热键配置");
    }

    /**
     * 获取热键配置
     */
    public HotkeyConfig getHotkeyConfig(String hotkeyId) {
        return hotkeyConfigs.get(hotkeyId);
    }

    /**
     * 获取所有热键配置
     */
    public Map<String, HotkeyConfig> getAllHotkeys() {
        return new HashMap<>(hotkeyConfigs);
    }

    /**
     * 检查条件
     */
    public boolean checkConditions(Player player, HotkeyConfig config) {
        return conditionChecker.checkAll(player, config);
    }

    /**
     * 执行命令
     */
    public void executeCommands(Player player, HotkeyConfig config) {
        for (String command : config.getCommands()) {
            try {
                String processedCommand = processPlaceholders(command, player);

                if (processedCommand.startsWith("[cmd]")) {
                    // 控制台命令
                    String consoleCommand = processedCommand.substring(5).trim();
                    Bukkit.getScheduler().runTask(plugin, () ->
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), consoleCommand)
                    );
                } else if (processedCommand.startsWith("[op]")) {
                    // OP 命令
                    String opCommand = processedCommand.substring(4).trim();
                    boolean wasOp = player.isOp();
                    try {
                        player.setOp(true);
                        Bukkit.getScheduler().runTask(plugin, () ->
                                player.performCommand(opCommand)
                        );
                    } finally {
                        player.setOp(wasOp);
                    }
                } else {
                    // 玩家命令
                    Bukkit.getScheduler().runTask(plugin, () ->
                            player.performCommand(processedCommand)
                    );
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING,
                        "执行命令失败: " + command + " 玩家: " + player.getName(), e);
            }
        }
    }

    /**
     * 处理占位符
     */
    private String processPlaceholders(String text, Player player) {
        return text.replace("%player%", player.getName())
                .replace("%uuid%", player.getUniqueId().toString())
                .replace("%world%", player.getWorld().getName())
                .replace("%x%", String.valueOf(player.getLocation().getBlockX()))
                .replace("%y%", String.valueOf(player.getLocation().getBlockY()))
                .replace("%z%", String.valueOf(player.getLocation().getBlockZ()));
    }

    /**
     * 重载配置
     */
    public void reload() {
        loadHotkeys();
    }
}