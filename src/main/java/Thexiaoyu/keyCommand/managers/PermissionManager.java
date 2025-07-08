package Thexiaoyu.keyCommand.managers;

import Thexiaoyu.keyCommand.Hotkey;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

public class PermissionManager {

    private final Hotkey plugin;
    private final Map<String, String> hotkeyPermissions = new HashMap<>();
    private boolean usePermissions;

    public PermissionManager(Hotkey plugin) {
        this.plugin = plugin;
        reload();
    }

    /**
     * 检查玩家是否有使用热键的权限
     */
    public boolean hasPermission(Player player, String hotkeyId) {
        if (!usePermissions) return true;

        String permission = hotkeyPermissions.getOrDefault(hotkeyId,
                "hotkey.use." + hotkeyId);

        return player.hasPermission(permission) ||
                player.hasPermission("hotkey.use.*") ||
                player.hasPermission("hotkey.*");
    }

    /**
     * 重载配置
     */
    public void reload() {
        usePermissions = plugin.getConfig().getBoolean("settings.use-permissions", true);
        hotkeyPermissions.clear();

        // 加载自定义权限
        if (plugin.getConfig().contains("permissions")) {
            plugin.getConfig().getConfigurationSection("permissions")
                    .getKeys(false).forEach(key ->
                            hotkeyPermissions.put(key, plugin.getConfig().getString("permissions." + key))
                    );
        }
    }
}