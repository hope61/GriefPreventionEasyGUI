package org.hope.griefPreventionEasyGUI.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.hope.griefPreventionEasyGUI.GriefPreventionEasyGUI;

public class PlayerJoinListener implements Listener {
    private final GriefPreventionEasyGUI plugin;

    public PlayerJoinListener(GriefPreventionEasyGUI plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.getConfig().getBoolean("onboarding-enabled", true)) return;

        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        if (plugin.getOnboarded().getBoolean("onboarded." + uuid, false)) return;

        long delayTicks = plugin.getConfig().getInt("onboarding-delay-seconds", 3) * 20L;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            String raw = plugin.getLang().getString("gui.messages.onboarding",
                    "Don't know how to protect your builds? Click here or type /claimhelp!");
            Component message = LegacyComponentSerializer.legacySection().deserialize(raw)
                    .clickEvent(ClickEvent.runCommand("/claimhelp"));

            player.sendMessage(message);
        }, delayTicks);
    }
}
