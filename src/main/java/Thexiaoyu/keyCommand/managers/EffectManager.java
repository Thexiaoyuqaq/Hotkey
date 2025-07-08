package Thexiaoyu.keyCommand.managers;

import Thexiaoyu.keyCommand.Hotkey;
import Thexiaoyu.keyCommand.HotkeyConfig;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import java.util.Map;
import java.util.logging.Level;

public class EffectManager {

    private final Hotkey plugin;
    private boolean soundEnabled;
    private boolean particlesEnabled;
    private boolean chatEnabled;
    private boolean actionBarEnabled;

    public EffectManager(Hotkey plugin) {
        this.plugin = plugin;
        reload();
    }

    /**
     * 播放效果
     */
    public void playEffects(Player player, HotkeyConfig config) {
        Location location = player.getLocation();
        Map<String, Object> effects = config.getEffects();

        // 播放声音
        if (soundEnabled && effects.containsKey("sound")) {
            playSound(player, location, (String) effects.get("sound"));
        }

        // 播放粒子效果
        if (particlesEnabled && effects.containsKey("particle")) {
            playParticle(player, location, (String) effects.get("particle"));
        }

        // 发送消息
        if (effects.containsKey("message")) {
            sendMessage(player, (String) effects.get("message"));
        }
    }

    /**
     * 播放声音
     */
    private void playSound(Player player, Location location, String soundName) {
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            float volume = (float) plugin.getConfig().getDouble("settings.effects.sound.volume", 1.0);
            float pitch = (float) plugin.getConfig().getDouble("settings.effects.sound.pitch", 1.0);
            player.playSound(location, sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, "无效的声音: " + soundName);
        }
    }

    /**
     * 播放粒子效果
     */
    private void playParticle(Player player, Location location, String particleName) {
        try {
            Particle particle = Particle.valueOf(particleName.toUpperCase());
            double spread = plugin.getConfig().getDouble("settings.effects.particles.spread", 0.5);
            int count = plugin.getConfig().getInt("settings.effects.particles.count", 10);

            location.add(0, 1, 0);
            player.getWorld().spawnParticle(particle, location, count, spread, spread, spread, 0);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, "无效的粒子效果: " + particleName);
        }
    }

    /**
     * 发送消息
     */
    private void sendMessage(Player player, String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);

        if (chatEnabled) {
            player.sendMessage(message);
        }

        if (actionBarEnabled) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent(message));
        }
    }

    /**
     * 重载配置
     */
    public void reload() {
        soundEnabled = plugin.getConfig().getBoolean("settings.effects.sound.enabled", true);
        particlesEnabled = plugin.getConfig().getBoolean("settings.effects.particles.enabled", true);
        chatEnabled = plugin.getConfig().getBoolean("settings.messages.chat", true);
        actionBarEnabled = plugin.getConfig().getBoolean("settings.messages.actionbar", true);
    }
}