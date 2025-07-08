package Thexiaoyu.keyCommand.managers;

import Thexiaoyu.keyCommand.Hotkey;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    private final Hotkey plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();
    private final boolean showActionBar;

    public CooldownManager(Hotkey plugin) {
        this.plugin = plugin;
        this.showActionBar = plugin.getConfig().getBoolean("settings.cooldown.show-actionbar", true);
    }

    /**
     * 检查并设置冷却
     */
    public boolean checkCooldown(Player player, String hotkeyId, double cooldownTime) {
        UUID playerId = player.getUniqueId();
        Map<String, Long> playerCooldowns = cooldowns.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());

        Long lastUse = playerCooldowns.get(hotkeyId);
        long currentTime = System.currentTimeMillis();

        if (lastUse != null) {
            long cooldownMillis = (long) (cooldownTime * 1000);
            long remainingTime = lastUse + cooldownMillis - currentTime;

            if (remainingTime > 0) {
                // 仍在冷却中
                sendCooldownMessage(player, remainingTime);
                return false;
            }
        }

        // 设置新的冷却时间
        playerCooldowns.put(hotkeyId, currentTime);
        return true;
    }

    /**
     * 发送冷却消息
     */
    private void sendCooldownMessage(Player player, long remainingMillis) {
        long remainingSeconds = (remainingMillis + 999) / 1000; // 向上取整
        String message = plugin.getLangManager().getMessage("cooldown",
                Map.of("time", String.valueOf(remainingSeconds)));

        if (showActionBar) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(message));
        } else {
            player.sendMessage(message);
        }
    }

    /**
     * 清理玩家数据
     */
    public void clearPlayer(UUID playerId) {
        cooldowns.remove(playerId);
    }

    /**
     * 清理过期的冷却数据
     */
    public void cleanup() {
        long currentTime = System.currentTimeMillis();
        cooldowns.forEach((playerId, playerCooldowns) -> {
            playerCooldowns.entrySet().removeIf(entry -> {
                return currentTime - entry.getValue() > 3600000;
            });
        });
        cooldowns.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

}