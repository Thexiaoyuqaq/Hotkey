package Thexiaoyu.keyCommand.listeners;

import Thexiaoyu.keyCommand.Hotkey;
import Thexiaoyu.keyCommand.HotkeyConfig;
import Thexiaoyu.keyCommand.managers.HotkeyManager;
import org.bukkit.entity.Player;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.block.Action;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HotkeyListener implements Listener {

    private final Hotkey plugin;
    private final HotkeyManager hotkeyManager;
    private final ConcurrentHashMap<UUID, ComboState> comboStates = new ConcurrentHashMap<>();

    // 配置值缓存
    private final long comboTimeout;
    private final boolean asyncCommands;
    private final long multiSneakTimeout;

    public HotkeyListener(Hotkey plugin) {
        this.plugin = plugin;
        this.hotkeyManager = plugin.getHotkeyManager();

        // 缓存配置值以提高性能
        this.comboTimeout = plugin.getConfig().getLong("settings.combo-timeout", 500);
        this.asyncCommands = plugin.getConfig().getBoolean("settings.async-commands", true);
        this.multiSneakTimeout = plugin.getConfig().getLong("settings.multi-sneak-timeout", 400);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        ComboState state = getOrCreateState(playerId);

        if (event.isSneaking()) {
            state.lastSneakTime = System.currentTimeMillis();
            // 重置组合键使用标记，避免与连击冲突
            state.comboUsed = false;
        } else {
            // 如果刚刚使用了组合键，跳过连击检测
            if (state.comboUsed) {
                state.comboUsed = false;
                state.reset();
                return;
            }

            // 检查多次潜行
            long currentTime = System.currentTimeMillis();

            // 如果距离上次释放 Shift 的时间在超时范围内，增加计数
            if (state.lastSneakRelease > 0 && currentTime - state.lastSneakRelease <= multiSneakTimeout) {
                state.sneakCount++;
                state.lastSneakRelease = currentTime; // 关键：更新时间戳

                // 显示按键计数反馈（可选）
                if (plugin.getConfig().getBoolean("settings.show-sneak-count", true)) {
                    showSneakCountFeedback(player, state.sneakCount);
                }

                // 设置延迟检查，等待可能的更多按键
                scheduleMultiSneakCheck(playerId, state.sneakCount);
            } else {
                // 第一次按下或超时后重新开始，重置计数
                state.sneakCount = 1;
                state.lastSneakRelease = currentTime;

                // 显示按键计数反馈
                if (plugin.getConfig().getBoolean("settings.show-sneak-count", true)) {
                    showSneakCountFeedback(player, 1);
                }

                // 设置延迟检查
                scheduleMultiSneakCheck(playerId, 1);
            }
        }
    }

    /**
     * 安排多次潜行检查任务
     */
    private void scheduleMultiSneakCheck(UUID playerId, int currentCount) {
        ComboState state = comboStates.get(playerId);
        if (state == null) return;

        // 取消之前的任务
        if (state.checkTask != null && !state.checkTask.isCancelled()) {
            state.checkTask.cancel();
        }

        // 创建新的检查任务
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                ComboState currentState = comboStates.get(playerId);
                if (currentState != null && currentState.sneakCount >= 2) {
                    Player player = plugin.getServer().getPlayer(playerId);
                    if (player != null) {
                        executeMultiSneak(player, currentState);
                    }
                    currentState.reset();
                }
            }
        };

        state.checkTask = runnable.runTaskLater(plugin, (multiSneakTimeout / 50) + 1);
    }

    /**
     * 执行多次潜行热键（优先执行最高次数）
     */
    private void executeMultiSneak(Player player, ComboState state) {
        for (int count = state.sneakCount; count >= 2; count--) {
            String hotkeyId = "double_sneak_" + count;
            HotkeyConfig config = hotkeyManager.getHotkeyConfig(hotkeyId);

            if (config != null && config.isEnabled()) {
                if (processHotkey(player, hotkeyId, null)) {
                    return;
                }
            }
        }
    }

    /**
     * 显示按键计数反馈
     */
    private void showSneakCountFeedback(Player player, int count) {
        String message = plugin.getConfig().getString("settings.sneak-count-format", "&e按键次数: &a%count%")
                .replace("%count%", String.valueOf(count));

        if (plugin.getConfig().getBoolean("settings.sneak-count-actionbar", true)) {
            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    new net.md_5.bungee.api.chat.TextComponent(
                            org.bukkit.ChatColor.translateAlternateColorCodes('&', message)));
        }

        // 播放声音
        if (plugin.getConfig().getBoolean("settings.sneak-count-sound", true)) {
            try {
                Sound sound = Sound.valueOf(plugin.getConfig().getString("settings.sneak-count-sound-type", "BLOCK_NOTE_BLOCK_PLING"));
                float pitch = 0.5f + (count * 0.2f); // 音调随次数增加
                player.playSound(player.getLocation(), sound, 0.5f, pitch);
            } catch (Exception ignored) {}
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerSwapHand(PlayerSwapHandItemsEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        ComboState state = comboStates.get(playerId);

        if (state != null && state.lastSneakTime > 0) {
            long timeDiff = System.currentTimeMillis() - state.lastSneakTime;
            if (timeDiff <= comboTimeout) {
                if (processHotkey(player, "shift_f", event)) {
                    event.setCancelled(true);
                    state.comboUsed = true;
                }
                state.lastSneakTime = 0;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getPlayer().isSneaking()) return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        ComboState state = comboStates.get(playerId);
        String hotkeyId = null;

        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            hotkeyId = "shift_left";
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            hotkeyId = "shift_right";
        }

        if (hotkeyId != null && processHotkey(player, hotkeyId, event)) {
            event.setCancelled(true);
            if (state != null) {
                state.comboUsed = true;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        ComboState state = comboStates.get(playerId);

        if (player.isSneaking()) {
            if (processHotkey(player, "shift_q", event)) {
                event.setCancelled(true);
                if (state != null) {
                    state.comboUsed = true;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        ComboState state = comboStates.remove(playerId);

        if (state != null && state.checkTask != null && !state.checkTask.isCancelled()) {
            state.checkTask.cancel();
        }

        plugin.getCooldownManager().clearPlayer(playerId);
    }

    /**
     * 处理热键执行
     */
    private boolean processHotkey(Player player, String hotkeyId, Object event) {
        HotkeyConfig config = hotkeyManager.getHotkeyConfig(hotkeyId);
        if (config == null || !config.isEnabled()) {
            return false;
        }

        // 权限检查
        if (!plugin.getPermissionManager().hasPermission(player, hotkeyId)) {
            player.sendMessage(plugin.getLangManager().getMessage("no_permission"));
            return false;
        }

        // 冷却检查
        if (!plugin.getCooldownManager().checkCooldown(player, hotkeyId, config.getCooldown())) {
            return false;
        }

        // 条件检查
        if (!hotkeyManager.checkConditions(player, config)) {
            return false;
        }

        // 执行命令
        if (asyncCommands && !config.isRequireSync()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    hotkeyManager.executeCommands(player, config);
                    // 异步播放效果
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            plugin.getEffectManager().playEffects(player, config);
                        }
                    }.runTask(plugin);
                }
            }.runTaskAsynchronously(plugin);
        } else {
            hotkeyManager.executeCommands(player, config);
            plugin.getEffectManager().playEffects(player, config);
        }

        return true;
    }

    private ComboState getOrCreateState(UUID playerId) {
        return comboStates.computeIfAbsent(playerId, k -> new ComboState());
    }

    /**
     * 组合键状态
     */
    private static class ComboState {
        long lastSneakTime = 0;
        long lastSneakRelease = 0;
        int sneakCount = 0;
        boolean comboUsed = false;
        BukkitTask checkTask = null;

        void reset() {
            lastSneakRelease = 0;
            sneakCount = 0;
            comboUsed = false;
            if (checkTask != null && !checkTask.isCancelled()) {
                checkTask.cancel();
                checkTask = null;
            }
        }
    }
}