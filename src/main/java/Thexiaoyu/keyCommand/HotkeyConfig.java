package Thexiaoyu.keyCommand;

import org.bukkit.configuration.ConfigurationSection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class HotkeyConfig {

    private final String id;
    private final boolean enabled;
    private final List<String> commands;
    private final double cooldown;
    private final boolean requireSync;
    private final Map<String, Object> conditions;
    private final Map<String, Object> effects;

    public HotkeyConfig(String id, ConfigurationSection section) {
        this.id = id;
        this.enabled = section.getBoolean("enabled", true);
        this.commands = section.getStringList("commands");
        this.cooldown = section.getDouble("conditions.cooldown", 3.0);
        this.requireSync = section.getBoolean("require-sync", false);

        // 加载条件
        this.conditions = new HashMap<>();
        ConfigurationSection conditionsSection = section.getConfigurationSection("conditions");
        if (conditionsSection != null) {
            for (String key : conditionsSection.getKeys(false)) {
                conditions.put(key, conditionsSection.get(key));
            }
        }

        // 加载效果
        this.effects = new HashMap<>();
        ConfigurationSection effectsSection = section.getConfigurationSection("effects");
        if (effectsSection != null) {
            for (String key : effectsSection.getKeys(false)) {
                effects.put(key, effectsSection.get(key));
            }
        }
    }

    // Getters
    public String getId() { return id; }
    public boolean isEnabled() { return enabled; }
    public List<String> getCommands() { return commands; }
    public double getCooldown() { return cooldown; }
    public boolean isRequireSync() { return requireSync; }
    public Map<String, Object> getConditions() { return conditions; }
    public Map<String, Object> getEffects() { return effects; }
}