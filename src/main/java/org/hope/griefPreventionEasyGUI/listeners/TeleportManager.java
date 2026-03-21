package org.hope.griefPreventionEasyGUI.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.hope.griefPreventionEasyGUI.GriefPreventionEasyGUI;

public class TeleportManager implements Listener {
    private final Player player;
    private final String cancelMessage;
    private final BukkitTask task;
    private volatile boolean done = false;

    public TeleportManager(GriefPreventionEasyGUI plugin, Player player, Location destination,
                           int delaySeconds, String cancelMessage, String successMessage) {
        this.player = player;
        this.cancelMessage = cancelMessage;

        Bukkit.getPluginManager().registerEvents(this, plugin);

        this.task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!done) {
                player.teleport(destination);
                player.sendMessage(successMessage);
                cleanup();
            }
        }, delaySeconds * 20L);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!event.getPlayer().getUniqueId().equals(player.getUniqueId())) return;
        if (done) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if ((int) from.getX() != (int) to.getX()
                || (int) from.getY() != (int) to.getY()
                || (int) from.getZ() != (int) to.getZ()) {
            player.sendMessage(cancelMessage);
            task.cancel();
            cleanup();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!event.getPlayer().getUniqueId().equals(player.getUniqueId())) return;
        if (done) return;
        task.cancel();
        cleanup();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!event.getEntity().getUniqueId().equals(player.getUniqueId())) return;
        if (done) return;
        task.cancel();
        cleanup();
    }

    private synchronized void cleanup() {
        if (done) return;
        done = true;
        HandlerList.unregisterAll(this);
    }
}
