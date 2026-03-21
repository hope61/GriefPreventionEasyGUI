package org.hope.griefPreventionEasyGUI.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hope.griefPreventionEasyGUI.GriefPreventionEasyGUI;
import org.hope.griefPreventionEasyGUI.GUIs.HelpMenuGUI;
import org.jetbrains.annotations.NotNull;

public class ClaimHelpCommand implements CommandExecutor {
    private final GriefPreventionEasyGUI plugin;

    public ClaimHelpCommand(GriefPreventionEasyGUI plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getLang().getString("gui.messages.players_only", "Only players can use this command."));
            return true;
        }

        // Mark player as onboarded on first use
        String key = "onboarded." + player.getUniqueId();
        if (!plugin.getOnboarded().getBoolean(key, false)) {
            plugin.getOnboarded().set(key, true);
            plugin.saveOnboardedConfig();
        }

        new HelpMenuGUI(plugin, player).getWindow().open();
        return true;
    }
}
