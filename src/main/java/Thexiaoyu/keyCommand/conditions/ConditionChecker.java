package Thexiaoyu.keyCommand.conditions;

import Thexiaoyu.keyCommand.Hotkey;
import Thexiaoyu.keyCommand.HotkeyConfig;
import org.bukkit.entity.Player;
import java.util.Map;

public class ConditionChecker {

    private final Hotkey plugin;

    public ConditionChecker(Hotkey plugin) {
        this.plugin = plugin;
    }

    /**
     * 检查所有条件
     */
    public boolean checkAll(Player player, HotkeyConfig config) {
        Map<String, Object> conditions = config.getConditions();

        // 检查世界
        if (conditions.containsKey("world")) {
            String requiredWorld = (String) conditions.get("world");
            if (!player.getWorld().getName().equalsIgnoreCase(requiredWorld)) {
                return false;
            }
        }

        // 检查游戏模式
        if (conditions.containsKey("gamemode")) {
            String requiredGamemode = conditions.get("gamemode").toString().toUpperCase();
            if (!player.getGameMode().toString().equals(requiredGamemode)) {
                return false;
            }
        }

        // 检查生命值
        if (conditions.containsKey("min-health")) {
            double minHealth = ((Number) conditions.get("min-health")).doubleValue();
            if (player.getHealth() < minHealth) {
                return false;
            }
        }

        // 检查饥饿值
        if (conditions.containsKey("min-food")) {
            int minFood = ((Number) conditions.get("min-food")).intValue();
            if (player.getFoodLevel() < minFood) {
                return false;
            }
        }

        // 检查经验等级
        if (conditions.containsKey("min-level")) {
            int minLevel = ((Number) conditions.get("min-level")).intValue();
            return player.getLevel() >= minLevel;
        }


        return true;
    }
}