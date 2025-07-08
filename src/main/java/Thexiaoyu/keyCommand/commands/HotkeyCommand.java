package Thexiaoyu.keyCommand.commands;

import Thexiaoyu.keyCommand.Hotkey;
import Thexiaoyu.keyCommand.HotkeyConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Map;

public class HotkeyCommand implements CommandExecutor {

    private final Hotkey plugin;

    public HotkeyCommand(Hotkey plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list":
                if (!checkPermission(sender, "hotkey.command.list")) return true;
                showHotkeyList(sender);
                break;

            case "reload":
                if (!checkPermission(sender, "hotkey.admin")) return true;
                plugin.reload();
                sender.sendMessage(plugin.getLangManager().getMessage("reload"));
                break;

            case "info":
                if (!checkPermission(sender, "hotkey.command.info")) return true;
                if (args.length < 2) {
                    sender.sendMessage(plugin.getLangManager().getMessage("usage.info"));
                    return true;
                }
                showHotkeyInfo(sender, args[1]);
                break;

            case "test":
                if (!checkPermission(sender, "hotkey.admin")) return true;
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.getLangManager().getMessage("player_only"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(plugin.getLangManager().getMessage("usage.test"));
                    return true;
                }
                testHotkey((Player) sender, args[1]);
                break;

            default:
                showHelp(sender);
                break;
        }
        return true;
    }

    private boolean checkPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(plugin.getLangManager().getMessage("no_permission"));
            return false;
        }
        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(plugin.getLangManager().getMessage("help.title"));
        sender.sendMessage(plugin.getLangManager().getMessage("help.list"));

        if (sender.hasPermission("hotkey.command.info")) {
            sender.sendMessage(plugin.getLangManager().getMessage("help.info"));
        }

        if (sender.hasPermission("hotkey.admin")) {
            sender.sendMessage(plugin.getLangManager().getMessage("help.reload"));
            sender.sendMessage(plugin.getLangManager().getMessage("help.test"));
        }
    }

    private void showHotkeyList(CommandSender sender) {
        Map<String, HotkeyConfig> hotkeys = plugin.getHotkeyManager().getAllHotkeys();

        if (hotkeys.isEmpty()) {
            sender.sendMessage(plugin.getLangManager().getMessage("list.empty"));
            return;
        }

        sender.sendMessage(plugin.getLangManager().getMessage("list.title"));

        hotkeys.forEach((id, config) -> {
            if (config.isEnabled()) {
                sender.sendMessage(plugin.getLangManager().getMessage("list.format",
                        Map.of("hotkey", id, "status", "§a启用")));
            }
        });
    }

    private void showHotkeyInfo(CommandSender sender, String hotkeyId) {
        HotkeyConfig config = plugin.getHotkeyManager().getHotkeyConfig(hotkeyId);

        if (config == null) {
            sender.sendMessage(plugin.getLangManager().getMessage("hotkey_not_found"));
            return;
        }

        sender.sendMessage(plugin.getLangManager().getMessage("info.title",
                Map.of("hotkey", hotkeyId)));
        sender.sendMessage(plugin.getLangManager().getMessage("info.status",
                Map.of("status", config.isEnabled() ? "§a启用" : "§c禁用")));
        sender.sendMessage(plugin.getLangManager().getMessage("info.cooldown",
                Map.of("cooldown", String.valueOf(config.getCooldown()))));

        sender.sendMessage(plugin.getLangManager().getMessage("info.commands"));
        for (String cmd : config.getCommands()) {
            sender.sendMessage("  §7- §f" + cmd);
        }
    }

    private void testHotkey(Player player, String hotkeyId) {
        HotkeyConfig config = plugin.getHotkeyManager().getHotkeyConfig(hotkeyId);

        if (config == null) {
            player.sendMessage(plugin.getLangManager().getMessage("hotkey_not_found"));
            return;
        }

        plugin.getHotkeyManager().executeCommands(player, config);
        plugin.getEffectManager().playEffects(player, config);
        player.sendMessage(plugin.getLangManager().getMessage("test_executed",
                Map.of("hotkey", hotkeyId)));
    }
}
